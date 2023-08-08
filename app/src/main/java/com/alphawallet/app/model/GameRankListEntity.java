package teleblock.model;

import java.util.List;

/**
 * Time:2023/5/5
 * Author:Perry
 * Description：游戏排行榜数据
 */
public class GameRankListEntity {

    private List<RankList> rank_list;
    private SelfRank self_rank;

    public List<RankList> getRank_list() {
        return rank_list;
    }

    public void setRank_list(List<RankList> rank_list) {
        this.rank_list = rank_list;
    }

    public SelfRank getSelf_rank() {
        return self_rank;
    }

    public void setSelf_rank(SelfRank self_rank) {
        this.self_rank = self_rank;
    }

    public class RankList {

        private int game_score;
        private int user_id;
        private String name;
        private String avatar;
        private String tg_user_id;
        private int rank;

        public int getGame_score() {
            return game_score;
        }

        public void setGame_score(int game_score) {
            this.game_score = game_score;
        }

        public int getUser_id() {
            return user_id;
        }

        public void setUser_id(int user_id) {
            this.user_id = user_id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getTg_user_id() {
            return tg_user_id;
        }

        public void setTg_user_id(String tg_user_id) {
            this.tg_user_id = tg_user_id;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }
    }

    public class SelfRank {
        private int rank;
        private int game_score;

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public int getGame_score() {
            return game_score;
        }

        public void setGame_score(int game_score) {
            this.game_score = game_score;
        }
    }

}
