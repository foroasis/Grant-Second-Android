package teleblock.model;

/**
 * Time:2022/12/6
 * Author:Perry
 * Description：创建红包返回
 */
public class CreateBonusEntity {
    private String amount;
    private String chain_id;
    private String chain_name;
    private int created_at;
    private String currency_id;
    private String currency_name;
    private int id;
    private String num;
    private String payment_account;
    private int receive_user_id;
    private String secret_num;
    private int source;
    private String tx_hash;
    private int user_id;
    private long expire_at;

    public void setExpire_at(long expire_at) {
        this.expire_at = expire_at;
    }

    public long getExpire_at() {
        return expire_at;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getChain_id() {
        return chain_id;
    }

    public void setChain_id(String chain_id) {
        this.chain_id = chain_id;
    }

    public String getChain_name() {
        return chain_name;
    }

    public void setChain_name(String chain_name) {
        this.chain_name = chain_name;
    }

    public int getCreated_at() {
        return created_at;
    }

    public void setCreated_at(int created_at) {
        this.created_at = created_at;
    }

    public String getCurrency_id() {
        return currency_id;
    }

    public void setCurrency_id(String currency_id) {
        this.currency_id = currency_id;
    }

    public String getCurrency_name() {
        return currency_name;
    }

    public void setCurrency_name(String currency_name) {
        this.currency_name = currency_name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getPayment_account() {
        return payment_account;
    }

    public void setPayment_account(String payment_account) {
        this.payment_account = payment_account;
    }

    public int getReceive_user_id() {
        return receive_user_id;
    }

    public void setReceive_user_id(int receive_user_id) {
        this.receive_user_id = receive_user_id;
    }

    public String getSecret_num() {
        return secret_num;
    }

    public void setSecret_num(String secret_num) {
        this.secret_num = secret_num;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public String getTx_hash() {
        return tx_hash;
    }

    public void setTx_hash(String tx_hash) {
        this.tx_hash = tx_hash;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}