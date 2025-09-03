package com.lwx.lwxmagiccodebackend.model.message;

import com.lwx.lwxmagiccodebackend.model.enums.StreamMessageTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * AI 响应消息
 */
@EqualsAndHashCode(callSuper = true)//确保父类的属性也参与equal的比较
@Data
@NoArgsConstructor
public class AiResponseMessage extends StreamMessage {

    private String data;

    public AiResponseMessage(String data) {
        super(StreamMessageTypeEnum.AI_RESPONSE.getValue());
        this.data = data;
    }
}
