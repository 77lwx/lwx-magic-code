package com.lwx.lwxmagiccodebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.RadixUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.lwx.lwxmagiccodebackend.constant.AppConstant;
import com.lwx.lwxmagiccodebackend.core.AiCodeGeneratorFacade;
import com.lwx.lwxmagiccodebackend.exception.BusinessException;
import com.lwx.lwxmagiccodebackend.exception.ErrorCode;
import com.lwx.lwxmagiccodebackend.exception.ThrowUtils;
import com.lwx.lwxmagiccodebackend.model.dto.app.AppQueryRequest;
import com.lwx.lwxmagiccodebackend.model.entity.User;
import com.lwx.lwxmagiccodebackend.model.enums.CodeGenTypeEnum;
import com.lwx.lwxmagiccodebackend.model.vo.AppVO;
import com.lwx.lwxmagiccodebackend.model.vo.UserVO;
import com.lwx.lwxmagiccodebackend.service.AppService;
import com.lwx.lwxmagiccodebackend.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.lwx.lwxmagiccodebackend.model.entity.App;
import com.lwx.lwxmagiccodebackend.mapper.AppMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/lilwx">浪味仙</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Override
    public Flux<String> chatToGenCode(Long appId, String userMessage ,User loginUser) {
        //1.校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(userMessage == null, ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        //2.查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR, "应用不存在");
        //3.验证权限，是否是本人应用
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "无权限访问应用");
        }
        //4.获取应用代码生成类别
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum enumByValue = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if(codeGenType == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码生成类别为空");
        }
        //5.调用代码生成器生成代码
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(userMessage, enumByValue,appId);
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        //1.校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf( loginUser == null, ErrorCode.PARAMS_ERROR, "用户未登录");
        //2.查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR, "应用不存在");
        //3.验证权限，是否是本人部署
        if ( !app.getUserId().equals(loginUser.getId())){
             throw new BusinessException(ErrorCode.SYSTEM_ERROR, "无权限部署应用");
        }
        //4.检查是否已经存在deploy key
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)){
            //没有deploy key 就生成6位（数字 ＋ 字母）
            deployKey= RandomUtil.randomNumbers(6);
        }
        //5.获取代码生成类型，构建文件目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        //6.检查源目录是否存在
        File sourceDir=new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码路径不存在，请先生成应用");
        }
        //7.复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用部署失败：" + e.getMessage());
        }
        //8.更新应用信息
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 9. 返回可访问的 URL 地址
        return String.format("%s/%s", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }


    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }



    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        QueryWrapper queryWrapper = QueryWrapper.create();

        queryWrapper = id != null ? queryWrapper.eq("id", id) : queryWrapper;
        queryWrapper = hasText(appName) ? queryWrapper.like("appName", appName) : queryWrapper;
        queryWrapper = hasText(cover) ? queryWrapper.like("cover", cover) : queryWrapper;
        queryWrapper = hasText(initPrompt) ? queryWrapper.like("initPrompt", initPrompt) : queryWrapper;

        // 关键：只有 codeGenType 非空才加条件
        queryWrapper = hasText(codeGenType) ? queryWrapper.eq("codeGenType", codeGenType) : queryWrapper;

        queryWrapper = hasText(deployKey) ? queryWrapper.eq("deployKey", deployKey) : queryWrapper;
        queryWrapper = priority != null ? queryWrapper.eq("priority", priority) : queryWrapper;
        queryWrapper = userId != null ? queryWrapper.eq("userId", userId) : queryWrapper;

        if (hasText(sortField)) {
            queryWrapper = queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        }

        return queryWrapper;
    }

    // 辅助方法
    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }

}
