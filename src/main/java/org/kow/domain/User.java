package org.kow.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String battleTag;
    private int compRank;
    private List<String> Most;
    private Tier tier;
    private Position position;
    private String group;
    private String overlogId;
}
