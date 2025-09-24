package com.chuadatten.notify.socket.messageType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PayUrl {
    String url;
    String type;


    public String toJson(){
        return "{\"url\":\""+url+"\",\"type\":\""+type+"\"}";
    }
}
