package com.robiulsunyemon.notification_service.notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;

@Data
@AllArgsConstructor
public class EmailMessage implements Serializable {
    private String toEmail;
    private String subject;
    private String body;
}