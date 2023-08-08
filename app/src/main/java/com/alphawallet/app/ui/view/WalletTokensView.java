package teleblock.ui.view;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.LayoutBaseRefreshBinding;
import org.telegram.messenger.databinding.ViewTokenEmptyBinding;

import java.util.List;

import teleblock.blockchain.BlockCallback;
import teleblock.blockchain.BlockFactory;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.TokenBalance;
import teleblock.ui.activity.WalletHomeAct;
import teleblock.ui.adapter.TokenBalanceAdapter;
import teleblock.util.MMKVUtil;
import teleblock.util.StringUtil;
import teleblock.widget.divider.CustomItemDecoration;

public class WalletTokensView extends FrameLayout implements OnRefreshListener, OnItemClickListener {

    private LayoutBaseRefreshBinding binding;
    private final WalletHomeAct walletHomeAct;
    private TokenBalanceAdapter tokenBalanceAdapter;

    public WalletTokensView(WalletHomeAct walletHomeAct) {
        super(walletHomeAct.getParentActivity());
        this.walletHomeAct = walletHomeAct;
        initView();
        initData();
    }

    private void initView() {
        binding = LayoutBaseRefreshBinding.inflate(LayoutInflater.from(getContext()), this, true);

    }

    private void initData() {
        binding.refreshLayout.setEnableLoadMore(false);
        binding.refreshLayout.setOnRefreshListener(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.addItemDecoration(new CustomItemDecoration(RecyclerView.VERTICAL, Color.parseColor("#E6E6E6"), 1f));
        tokenBalanceAdapter = new TokenBalanceAdapter();
        tokenBalanceAdapter.setEmptyView(createEmptyView());
        tokenBalanceAdapter.getEmptyLayout().setVisibility(View.GONE);
        tokenBalanceAdapter.setOnItemClickListener(this);
        binding.recyclerView.setAdapter(tokenBalanceAdapter);
        binding.refreshLayout.autoRefresh();
    }

    private View createEmptyView() {
        ViewTokenEmptyBinding binding = ViewTokenEmptyBinding.inflate(LayoutInflater.from(getContext()));
        binding.tvEmptyToken.setText(LocaleController.getString("wallet_home_token_empty_text", R.string.wallet_home_token_empty_text));
        return binding.getRoot();
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        loadData();
    }

    private void loadData() {
        walletHomeAct.binding.tvTotalAmount.setText("US$" + StringUtil.formatPrice(0, true));
        Web3ConfigEntity.WalletNetworkConfigChainType chainType = MMKVUtil.currentChainConfig();
        BlockFactory.get(chainType.getId()).getTokenList(walletHomeAct.address, new BlockCallback<List<TokenBalance>>() {
            @Override
            public void onSuccess(List<TokenBalance> data) {
                double totalAmount = 0;
                for (TokenBalance tokenBalance : data) {
                    totalAmount += tokenBalance.balanceUSD;
                }
                walletHomeAct.binding.tvTotalAmount.setText("US$" + StringUtil.formatPrice(totalAmount, true));
                tokenBalanceAdapter.setList(data);
                binding.refreshLayout.finishRefresh();
                if (tokenBalanceAdapter.getData().isEmpty()) {
                    tokenBalanceAdapter.getEmptyLayout().setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String msg) {
                binding.refreshLayout.finishRefresh();
                tokenBalanceAdapter.getEmptyLayout().setVisibility(View.VISIBLE);
            }
        });


//        String symbol = chainType.getMain_currency_name();
//        HttpRequest httpRequest = null;
//        if ("TT".equalsIgnoreCase(symbol)) {
//            httpRequest = EasyHttp.post(new ApplicationLifecycle())
//                    .api(new TTTokensApi())
//                    .body(new JsonBody(TTTokensApi.createJson(walletHomeAct.address)));
//            // 请求币种单价
//            ttTokensPrice = new HashMap<>();
//            WalletUtil.requestMainCoinPrice("thunder-token", new WalletUtil.RequestCoinPriceListener() {
//                @Override
//                public void requestEnd() {
//                    getTTTokensPrice();
//                }
//
//                @Override
//                public void requestError(String msg) {
//                }
//
//                @Override
//                public void requestSuccessful(CurrencyPriceEntity resultData) {
//                    ttTokensPrice.put("TT", resultData.getUsd());
//                }
//            });
//        } else if ("ROSE".equalsIgnoreCase(symbol)) {
//            httpRequest = EasyHttp.get(new ApplicationLifecycle())
//                    .api(new OasisTokensApi()
//                            .setAddress(walletHomeAct.address));
//            // 请求币种单价
//            oasisTokensPrice = new HashMap<>();
//            WalletUtil.requestMainCoinPrice("oasis-network", new WalletUtil.RequestCoinPriceListener() {
//                @Override
//                public void requestEnd() {
//                    getOasisTokensPrice();
//                }
//
//                @Override
//                public void requestError(String msg) {
//                }
//
//                @Override
//                public void requestSuccessful(CurrencyPriceEntity resultData) {
//                    oasisTokensPrice.put("ROSE", resultData.getUsd());
//                }
//            });
//            getOasisBalance();
//        } else {
//            httpRequest = EasyHttp.get(new ApplicationLifecycle())
//                    .api(new TokenBalancesApi()
//                            .setAddresses(walletHomeAct.address)
//                            .setNetwork(symbol));
//        }
//        httpRequest.request(new OnHttpListener<String>() {
//            @Override
//            public void onSucceed(String result) {
//                double totalAmount = 0;
//                if ("TT".equalsIgnoreCase(symbol)) {
//                    List<JsonRpc> jsonRpcList = JsonUtil.parseJsonToList(result, JsonRpc.class);
//                    List<TTToken> tokenList = MMKVUtil.getTTTokens();
//                    // 添加主币数据
//                    TTToken ttToken = new TTToken();
//                    ttToken.setImage("https://ttswap.space/static/media/tt.e15cb968.png");
//                    ttToken.setSymbol("TT");
//                    ttToken.setDecimals(18);
//                    tokenList.add(0, ttToken);
//                    List<TokenBalance> finalTokenBalances = tokenBalances;
//                    CollectionUtils.forAllDo(tokenList, new CollectionUtils.Closure<TTToken>() {
//                        @Override
//                        public void execute(int index, TTToken item) {
//                            String balance;
//                            try {
//                                balance = Numeric.toBigInt(jsonRpcList.get(index).getResult()).toString();
//                            } catch (Exception e) {
//                                balance = "0";
//                            }
//                            item.setBalance(balance);
//                            if (ttTokensPrice != null && ttTokensPrice.get(item.getSymbol()) != null) {
//                                item.setPrice(ttTokensPrice.get(item.getSymbol()));
//                            }
//                            finalTokenBalances.add(TokenBalance.parse(item));
//                        }
//                    });
//                    // 过滤掉没余额的代币
//                    CollectionUtils.filter(tokenBalances, new CollectionUtils.Predicate<TokenBalance>() {
//                        @Override
//                        public boolean evaluate(TokenBalance item) {
//                            return Double.parseDouble(item.balance) > 0;
//                        }
//                    });
//                    // 计算总资产
//                    for (TokenBalance tokenBalance : tokenBalances) {
//                        totalAmount += tokenBalance.balanceUSD;
//                    }
//                } else if ("ROSE".equalsIgnoreCase(symbol)) {
//                    List<OasisToken> tokenList = JsonUtil.parseJsonToList(JsonUtils.getString(result, "result"), OasisToken.class);
//                    for (OasisToken oasisToken : tokenList) {
//                        if (!"ERC-20".equals(oasisToken.getType())) continue;
//                        if (oasisTokensPrice != null && oasisTokensPrice.get(oasisToken.getSymbol()) != null) {
//                            oasisToken.setPrice(oasisTokensPrice.get(oasisToken.getSymbol()));
//                        }
//                        String drawableName = "ic_os_" + oasisToken.getSymbol().toLowerCase();
//                        if (ResourceUtils.getDrawableIdByName(drawableName) > 0) {
//                            oasisToken.setImageRes(ResourceUtils.getDrawableIdByName(drawableName));
//                        }
//                        tokenBalances.add(TokenBalance.parse(oasisToken));
//                    }
//                    // 计算总资产
//                    for (TokenBalance tokenBalance : tokenBalances) {
//                        totalAmount += tokenBalance.balanceUSD;
//                    }
//                } else {
//                    EthBalances ethBalances = EthBalances.parse(result, walletHomeAct.address.toLowerCase());
//                    if (ethBalances != null) {
//                        tokenBalances = TokenBalance.parse(ethBalances);
//                        totalAmount = ethBalances.value;
//                    }
//                }
//                tokenBalanceAdapter.setList(tokenBalances);
//                walletHomeAct.binding.tvTotalAmount.setText("US$" + StringUtil.formatPrice(totalAmount, true));
//                binding.refreshLayout.finishRefresh();
//                if (tokenBalanceAdapter.getData().isEmpty()) {
//                    tokenBalanceAdapter.getEmptyLayout().setVisibility(View.VISIBLE);
//                }
//            }
//
//            @Override
//            public void onFail(Exception e) {
//                binding.refreshLayout.finishRefresh();
//                tokenBalanceAdapter.getEmptyLayout().setVisibility(View.VISIBLE);
//            }
//        });
    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
    }

}