package teleblock.model;

/**
 * Time:2023/5/5
 * Author:Perry
 * Description：游戏上报结果
 */
public class GameUploadResultEntity {
    private int rank;
    private int up;
    private int token;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getUp() {
        return up;
    }

    public void setUp(int up) {
        this.up = up;
    }

    public int getToken() {
        return token;
    }

    public void setToken(int token) {
        this.token = token;
    }
}
