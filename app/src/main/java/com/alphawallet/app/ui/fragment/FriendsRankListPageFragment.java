package teleblock.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.CollectionUtils;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.databinding.FragmentFriendsRankListBinding;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import teleblock.model.GameRankListEntity;
import teleblock.model.ui.GameInviteFriendsEntity;
import teleblock.ui.adapter.GameInviteFriendsAdapter;
import teleblock.util.EventUtil;
import teleblock.util.parse.MiniGamesInviteParseUtil;

/**
 * Time:2023/5/6
 * Author:Perry
 * Description：游戏邀请朋友页面
 */
public class FriendsRankListPageFragment extends BaseFragment{
    private FragmentFriendsRankListBinding binding;
    private GameInviteFriendsAdapter mGameInviteFriendsAdapter;

    public static FriendsRankListPageFragment instance() {
        FriendsRankListPageFragment fragment = new FriendsRankListPageFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View getFrameLayout(LayoutInflater inflater) {
        binding = FragmentFriendsRankListBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void onViewCreated() {
        binding.rv.setLayoutManager(new LinearLayoutManager(getContext()));
        mGameInviteFriendsAdapter = new GameInviteFriendsAdapter();
        binding.rv.setAdapter(mGameInviteFriendsAdapter);

        mGameInviteFriendsAdapter.setOnItemClickListener((adapter, view, position) -> {
            GameInviteFriendsEntity clickData = mGameInviteFriendsAdapter.getItem(position);
            if (clickData.isIfInvite()) {
                return;
            }
            EventUtil.track(getContext(), EventUtil.Even.游戏排行榜邀请点击, new HashMap<>());

            TLRPC.User user = clickData.getUser();
            String data = MiniGamesInviteParseUtil.setParseStr(user.id, UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId());
            SendMessagesHelper.getInstance(UserConfig.selectedAccount).sendMessage(data, user.id, null, null, null, true, null, null, null, true, 0, null,false);
            mGameInviteFriendsAdapter.click(position);
        });

        //联系人数据
        ArrayList<TLRPC.TL_contact> contactsList = AccountInstance.getInstance(UserConfig.selectedAccount).getContactsController().contacts;
        //榜单数据
        List<GameRankListEntity.RankList> allRankList = getMessageController().rankLists;

        //联系人用户数据
        ArrayList<GameInviteFriendsEntity> users = new ArrayList<>();

        if (CollectionUtils.isEmpty(allRankList)) {
            for (TLRPC.TL_contact tl_contact : contactsList) {
                TLRPC.User user = getMessageController().getUser(tl_contact.user_id);
                if (!UserObject.isDeleted(user) && !UserObject.isUserSelf(user)) {
                    GameInviteFriendsEntity item = new GameInviteFriendsEntity();
                    item.setUser(user);
                    item.setIfInvite(false);
                    users.add(item);
                }
            }
            mGameInviteFriendsAdapter.setList(users);
            return;
        }

        for (TLRPC.TL_contact tl_contact : contactsList) {
            boolean has = false;
            for (GameRankListEntity.RankList rankList : allRankList) {
                if (rankList.getTg_user_id().equals(String.valueOf(tl_contact.user_id))) {
                    has = true;
                    break;
                }
            }

            if (!has) {
                TLRPC.User user = getMessageController().getUser(tl_contact.user_id);
                if (!UserObject.isDeleted(user) && !UserObject.isUserSelf(user)) {
                    GameInviteFriendsEntity item = new GameInviteFriendsEntity();
                    item.setUser(user);
                    item.setIfInvite(false);
                    users.add(item);
                }
            }
        }

        mGameInviteFriendsAdapter.setList(users);
    }

    private MessagesController getMessageController() {
        return MessagesController.getInstance(UserConfig.selectedAccount);
    }

}
