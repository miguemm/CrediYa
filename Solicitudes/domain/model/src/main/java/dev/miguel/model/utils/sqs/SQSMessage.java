package dev.miguel.model.utils.sqs;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
public class SQSMessage {
    Long solicitudId;
    String correoElectronico;
    String estado;
}
