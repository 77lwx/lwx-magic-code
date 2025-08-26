package com.lwx.lwxmagiccodebackend.service;

import com.lwx.lwxmagiccodebackend.model.dto.app.AppQueryRequest;
import com.lwx.lwxmagiccodebackend.model.entity.User;
import com.lwx.lwxmagiccodebackend.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.lwx.lwxmagiccodebackend.model.entity.App;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/77lwx">浪味仙</a>
 */
public interface AppService extends IService<App> {


    AppVO getAppVO(App app);

    List<AppVO> getAppVOList(List<App> appList);

    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    Flux<String> chatToGenCode(Long appId, String userMessage, User loginUser);

    String deployApp(Long appId, User loginUser);
}
