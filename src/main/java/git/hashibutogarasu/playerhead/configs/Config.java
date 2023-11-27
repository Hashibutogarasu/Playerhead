package git.hashibutogarasu.playerhead.configs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import git.hashibutogarasu.playerhead.ListType;

import java.util.ArrayList;
import java.util.List;

public class Config {
    @SerializedName("favorited_players")
    @Expose
    public List<String> favorited_players = new ArrayList<>();

    @SerializedName("tutorial_flag")
    @Expose
    public boolean tutorial_flag = false;


    @SerializedName("last_listtype")
    @Expose
    public ListType last_listtype = ListType.SERVER_PLAYERS;
}
