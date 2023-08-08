package teleblock.model;

/**
 * Time:2023/3/31
 * Author:Perry
 * Description：
 */
public class HashPayStatusEntity {
    private String tx_hash;
    private int status;

    public String getTx_hash() {
        return tx_hash;
    }

    public void setTx_hash(String tx_hash) {
        this.tx_hash = tx_hash;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
