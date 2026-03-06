package eu.xap3y.gungame.api.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardConfig {

    private String title;
    private List<String> lines;

}