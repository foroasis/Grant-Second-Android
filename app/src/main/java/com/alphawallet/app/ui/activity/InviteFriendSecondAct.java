package teleblock.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.http.EasyHttp;
import com.hjq.http.config.IRequestApi;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.databinding.ActInviteFriendSecondBinding;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import teleblock.config.AppConfig;
import teleblock.model.InviteConfigEntity;
import teleblock.model.InviteFriendEntity;
import teleblock.network.BaseBean;
import teleblock.network.api.InviteAgainApi;
import teleblock.network.api.InviteCreateApi;
import teleblock.network.api.InviteUsersApi;
import teleblock.ui.adapter.InviteFriendAdapter;
import teleblock.ui.dialog.BaseAlertDialog;
import teleblock.ui.dialog.InviteFriendConfirmDialog;
import teleblock.util.EventUtil;
import teleblock.util.WalletDaoUtils;

/**
 * 红包拉新-第二步
 */
public class InviteFriendSecondAct extends BaseFragment implements OnItemClickListener {

    private ActInviteFriendSecondBinding binding;
    private InviteFriendAdapter inviteFriendAdapter;
    private List<TLRPC.User> users = new ArrayList<>();
    private List<Long> ids = new ArrayList<>();
    private InviteConfigEntity.Level level;
    private String promotion_number;
    private Runnable runnable;

    public InviteFriendSecondAct(Bundle args, Runnable runnable) {
        super(args);
        this.runnable = runnable;
    }

    @Override
    public boolean onFragmentCreate() {
        level = (InviteConfigEntity.Level) getArguments().getSerializable("level");
        promotion_number = getArguments().getString("promotion_number");
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setAddToContainer(false);
        binding = ActInviteFriendSecondBinding.inflate(LayoutInflater.from(context));
        initView();
        initData();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.rvInviteFriend.setLayoutManager(new LinearLayoutManager(getContext()));
        inviteFriendAdapter = new InviteFriendAdapter();
        inviteFriendAdapter.setOnItemClickListener(this);
        binding.rvInviteFriend.setAdapter(inviteFriendAdapter);

        binding.ivInviteBack.setOnClickListener(view -> finishFragment());
        binding.tvInviteNext.setOnClickListener(v -> {
            if (users.size() < level.numbers) {
                ToastUtils.showLong(LocaleController.getString("invite_friend_second_not_enough_tip", R.string.invite_friend_second_not_enough_tip));
                return;
            }
            new InviteFriendConfirmDialog(getContext(), level, users, data -> {
                EventUtil.track(getContext(), EventUtil.Even.红包确认发送按钮点击, new HashMap<>());
                createInvite();
            }).show();
        });

        String text = LocaleController.getString("invite_friend_second_title", R.string.invite_friend_second_title);
        if (!TextUtils.isEmpty(promotion_number)) text = text.split("\n")[1];
        binding.tvInviteLevelTitle.setText(text);
        binding.tvInviteNext.setText(LocaleController.getString("invite_friend_second_no_selected", R.string.invite_friend_second_no_selected));
        binding.tvInviteNext.setEnabled(false);
    }

    private void createInvite() {
        AlertDialog progressDialog = new AlertDialog(getContext(), 3);
        progressDialog.setCanCancel(false);
        showDialog(progressDialog);
        IRequestApi requestApi;
        if (!TextUtils.isEmpty(promotion_number)) {
            requestApi = new InviteAgainApi()
                    .setPromotion_number(promotion_number)
                    .setUsers(ids);
        } else {
            requestApi = new InviteCreateApi()
                    .setLevel_id(level.id)
                    .setAmount(level.amount)
                    .setUsers_number(level.numbers)
                    .setUsers(ids)
                    .setReceipt_account(WalletDaoUtils.getCurrent().getAddress());
        }
        EasyHttp.post(new ApplicationLifecycle())
                .api(requestApi)
                .request(new OnHttpListener<BaseBean<String>>() {
                    @Override
                    public void onSucceed(BaseBean<String> result) {
                        String userName = UserObject.getUserName(getUserConfig().getCurrentUser());
                        for (Long id : ids) { // 发送给好友
                            long dialogId = Math.abs(id);
                            String message = String.format(LocaleController.getString("view_chat_item_pullnew_bouns_invite_text", R.string.view_chat_item_pullnew_bouns_invite_text), userName, level.amount)
                                    + "\n" + AppConfig.NetworkConfig.ALPHA_DOWNLOAD_LINK;
                            getSendMessagesHelper().sendMessage(message, dialogId, null, null, null, true, null, null, null, true, 0, null, false);
                        }
                        finishFragment();
                        AndroidUtilities.runOnUIThread(() -> runnable.run(), 200);
                    }

                    @Override
                    public void onFail(Exception e) {
                        ToastUtils.showLong(e.getMessage());
                    }

                    @Override
                    public void onEnd(Call call) {
                        if (getVisibleDialog() != null) {
                            getVisibleDialog().dismiss();
                        }
                    }
                });
    }

    private void initData() {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) binding.llInviteTop.getLayoutParams();
        layoutParams.topMargin = AndroidUtilities.statusBarHeight;
        loadData();
    }

    private void loadData() {
        ArrayList<TLRPC.User> contacts = new ArrayList<>();
        ArrayList<Long> ids = new ArrayList<>();
        ArrayList<TLRPC.TL_contact> arrayList = getContactsController().contacts;
        try {
            int currentTime = ConnectionsManager.getInstance(currentAccount).getCurrentTime();
            Collections.sort(arrayList, (o1, o2) -> {
                TLRPC.User user1 = getMessagesController().getUser(o2.user_id);
                TLRPC.User user2 = getMessagesController().getUser(o1.user_id);
                int status1 = 0;
                int status2 = 0;
                if (user1 != null) {
                    if (user1.self) {
                        status1 = currentTime + 50000;
                    } else if (user1.status != null) {
                        status1 = user1.status.expires;
                    }
                }
                if (user2 != null) {
                    if (user2.self) {
                        status2 = currentTime + 50000;
                    } else if (user2.status != null) {
                        status2 = user2.status.expires;
                    }
                }
                if (status1 > 0 && status2 > 0) {
                    if (status1 > status2) {
                        return 1;
                    } else if (status1 < status2) {
                        return -1;
                    }
                    return 0;
                } else if (status1 < 0 && status2 < 0) {
                    if (status1 > status2) {
                        return 1;
                    } else if (status1 < status2) {
                        return -1;
                    }
                    return 0;
                } else if (status1 < 0 && status2 > 0 || status1 == 0 && status2 != 0) {
                    return -1;
                } else if (status2 < 0 && status1 > 0 || status2 == 0 && status1 != 0) {
                    return 1;
                }
                return 0;
            });
        } catch (Exception e) {
            FileLog.e(e);
        }

        for (int a = 0; a < arrayList.size(); a++) {
            TLRPC.User user = getMessagesController().getUser(arrayList.get(a).user_id);
            if (user == null || user.self || user.deleted) {
                continue;
            }
            contacts.add(user);
            ids.add(user.id);
        }
        EasyHttp.post(new ApplicationLifecycle())
                .api(new InviteUsersApi()
                        .setPromotion_number(level.id)
                        .setTg_user_id(ids))
                .request(new OnHttpListener<BaseBean<List<InviteFriendEntity>>>() {

                    @Override
                    public void onSucceed(BaseBean<List<InviteFriendEntity>> result) {
                        List<InviteFriendEntity> list = result.getData();
                        List<InviteFriendEntity> friends = new ArrayList<>();
                        for (TLRPC.User user : contacts) {
                            InviteFriendEntity friend = CollectionUtils.find(list, item -> item.tg_user_id == user.id);
                            if (friend == null) {
                                friend = new InviteFriendEntity();
                            } else if (friend.status == 2) {
                                continue; // 过滤已领取
                            }
                            friend.tg_user_id = user.id;
                            friend.user = user;
                            friends.add(friend);
                        }
                        inviteFriendAdapter.setList(friends);
                    }

                    @Override
                    public void onFail(Exception e) {
                        ToastUtils.showLong(e.getMessage());
                        finishFragment();
                    }

                    @Override
                    public void onEnd(Call call) {
                        binding.animationView.cancelAnimation();
                        binding.animationView.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
        InviteFriendEntity inviteFriend = inviteFriendAdapter.getItem(position);
        if (inviteFriend.status == 1) return;
        inviteFriend.checked = !inviteFriend.checked;
        inviteFriendAdapter.setData(position, inviteFriend);
        if (inviteFriend.checked) {
            users.add(inviteFriend.user);
            ids.add(inviteFriend.tg_user_id);
        } else {
            users.remove(inviteFriend.user);
            ids.remove(inviteFriend.tg_user_id);
        }
        if (users.isEmpty()) {
            binding.tvInviteNext.setText(LocaleController.getString("invite_friend_second_no_selected", R.string.invite_friend_second_no_selected));
            binding.tvInviteNext.setEnabled(false);
        } else {
            binding.tvInviteNext.setText(LocaleController.getString("invite_friend_second_next_text", R.string.invite_friend_second_next_text));
            binding.tvInviteNext.setEnabled(true);
        }
    }
}
