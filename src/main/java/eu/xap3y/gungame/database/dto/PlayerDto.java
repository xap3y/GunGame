package eu.xap3y.gungame.database.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class PlayerDto {

    int id;
    UUID uuid;
    String name;
    PlayerStatsDto stats;
}
