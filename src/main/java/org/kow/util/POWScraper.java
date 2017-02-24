package org.kow.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.kow.domain.Position;
import org.kow.domain.Tier;
import org.kow.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class POWScraper {
    private static final Logger logger = LoggerFactory.getLogger(POWScraper.class);

    public static User getUser(String battleTag) throws IOException {

        User user = new User();
        user.setBattleTag(battleTag.replace("-", "#"));

        battleTag = battleTag.replace("#", "-");
        String powUrl = "https://playoverwatch.com/ko-kr/career/pc/kr/";
        Document doc = Jsoup.connect(powUrl + battleTag).get();

        try {
            // comp rank
            Element compRank = doc.select("div > div.competitive-rank > div").first();
            user.setCompRank(Integer.parseInt(compRank.text()));

            // most 3
            List<String> most = new ArrayList<>();
            Elements playTime = doc.select("#competitive > section.content-box.u-max-width-container.hero-comparison-section > div > div.progress-category.is-partial.toggle-display.is-active > div > div.bar-container > div.bar-text");
            for (int i = 0; i < 3; i++) {
                Element e = playTime.get(i);
                most.add(e.child(0).text());
            }
            user.setMost(most);

            // tier
            Elements tierElem = doc.select("#overview-section > div > div.u-max-width-container.row.content-box.gutter-18 > div > div > div.masthead-player > div > div.competitive-rank > img");
            user.setTier(getTier(tierElem.first().attr("src")));


            // position
            user.setPosition(getPlayerPosition(playTime));
        } catch (Exception e) {
            user.setCompRank(0);
            user.setMost(new ArrayList<>());
            user.setPosition(Position.NONE);
            user.setTier(Tier.NONE);
        }

        return user;
    }

    private static Tier getTier(String fromString) {
        if (fromString.contains("rank-7.png")) {
            return Tier.GRANDMASTER;
        } else if (fromString.contains("rank-6.png")) {
            return Tier.MASTER;
        } else if (fromString.contains("rank-6.png")) {
            return Tier.MASTER;
        } else if (fromString.contains("rank-5.png")) {
            return Tier.DIAMOND;
        } else if (fromString.contains("rank-4.png")) {
            return Tier.PLATINUM;
        } else if (fromString.contains("rank-3.png")) {
            return Tier.GOLD;
        } else if (fromString.contains("rank-2.png")) {
            return Tier.SILVER;
        } else {
            return Tier.BRONZE;
        }
    }

    // TODO: Flex 판별 룰은 나주에 좀 더 상세하게.
    private static Position getPlayerPosition(Elements playTimeList) {
        Map<Position, Integer> PositionCount = new HashMap<>();
        PositionCount.put(Position.TANK, 0);
        PositionCount.put(Position.DPS, 0);
        PositionCount.put(Position.SUPPORT, 0);
        PositionCount.put(Position.DEFENCE, 0);
        PositionCount.put(Position.FLEX, 0);

        int totalPlaying = 0;
        for (Element e : playTimeList) {
            int playingTime = getPlayingTimeBy(e.child(1).text());
            totalPlaying += playingTime;

            Position po = getPositionBy(e.child(0).text());
            PositionCount.put(po, PositionCount.get(po) + playingTime);
        }

        for (Position po : PositionCount.keySet()) {
            double playingPer = PositionCount.get(po) * 100 / totalPlaying;
            if (playingPer > 50) {
                return po;
            }
        }

        return Position.FLEX;
    }

    private static int getPlayingTimeBy(String text) {
        if (text.equals("--")) {
            return 0;
        }

        int factor;
        String[] splitText = text.split("\\s");
        switch (splitText[1]) {
            case "시간":
                factor = 60 * 60;
                break;
            case "분":
                factor = 60;
                break;
            case "초":
                factor = 1;
                break;
            default:
                return 0;
        }
        int displayTime = Integer.parseInt(splitText[0]);
        return displayTime * factor;
    }

    private static Position getPositionBy(String heroName) {
        switch (heroName) {
            case "자리야":
            case "라인하르트":
            case "D.Va":
            case "윈스턴":
            case "로드호그":
                return Position.TANK;

            case "루시우":
            case "아나":
            case "메르시":
            case "시메트라":
            case "젠야타":
                return Position.SUPPORT;

            case "솔저: 76":
            case "파라":
            case "리퍼":
            case "솜브라":
            case "트레이서":
            case "겐지":
            case "맥크리":
                return Position.DPS;

            case "토르비욘":
            case "정크랫":
            case "한조":
            case "바스티온":
            case "위도우메이커":
            case "메이":
                return Position.DEFENCE;

            default:
                return Position.FLEX;
        }
    }
}
