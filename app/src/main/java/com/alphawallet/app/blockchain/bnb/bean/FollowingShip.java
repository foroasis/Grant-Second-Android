package teleblock.blockchain.bnb.bean;

/**
 * 创建日期：2023/3/1
 * 描述：关注关系
 */
public class FollowingShip {

    private Node node;

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public static class Node {
        private Profile profile;

        public Profile getProfile() {
            return profile;
        }

        public void setProfile(Profile profile) {
            this.profile = profile;
        }

        public static class Profile {
            private String handle;
            private Owner owner;

            public String getHandle() {
                return handle.replace(".cc", "");
            }

            public void setHandle(String handle) {
                this.handle = handle;
            }

            public Owner getOwner() {
                return owner;
            }

            public void setOwner(Owner owner) {
                this.owner = owner;
            }

            public static class Owner {
                private String address;

                public String getAddress() {
                    return address;
                }

                public void setAddress(String address) {
                    this.address = address;
                }
            }
        }
    }
}