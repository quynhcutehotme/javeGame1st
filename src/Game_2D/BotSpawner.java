package Game_2D;

import entity.WorldItem;
import entity.bot;

public class BotSpawner {

    private final gamePanel gp;

    public BotSpawner(gamePanel gp) {
        this.gp = gp;
    }

    public void spawnBotsFixed() {
        gp.bots.clear();

        // house giữ nguyên
        gp.house = new WorldItem(gp, gp.tileSize * 1, gp.tileSize * 8 + 20);

        int y = gp.getBotSpawnY(); // giữ nguyên cách tính Y

        bot b1 = new bot(gp, gp.tileSize * 10, y, bot.BotType.GREEN);
        b1.direction = "left";
        b1.setPatrolRangePx(gp.tileSize * 4);

        bot b2 = new bot(gp, gp.tileSize * 25, y, bot.BotType.PURPLE);
        b2.direction = "right";
        b2.setPatrolRangePx(gp.tileSize * 3);

        bot b3 = new bot(gp, gp.tileSize * 38, y, bot.BotType.YELLOW);
        b3.direction = "left";
        b3.setPatrolRangePx(gp.tileSize * 1);

        bot b4 = new bot(gp, gp.tileSize * 44, y, bot.BotType.GREEN);
        b4.direction = "right";
        b4.setPatrolRangePx(gp.tileSize * 2);

        bot b5 = new bot(gp, gp.tileSize * 60, y, bot.BotType.PURPLE);
        b5.direction = "right";
        b5.setPatrolRangePx(gp.tileSize * 4);

        bot b6 = new bot(gp, gp.tileSize * 70, y, bot.BotType.YELLOW);
        b6.direction = "left";
        b6.setPatrolRangePx(gp.tileSize * 4);

        gp.bots.add(b1);
        gp.bots.add(b2);
        gp.bots.add(b3);
        gp.bots.add(b4);
        gp.bots.add(b5);
        gp.bots.add(b6);

        // chặn spawn khác
        gp.initialBotsSpawned = 3;
        gp.lastInitialBotSpawnTime = 0;
    }
}
