package krash220.xbob.game.api.mod;

public class TacZ {

    private static Boolean tacz = null;

    private static void initTacZ()
    {
        if (tacz == null) {
            try {
                Class.forName("com.tacz.guns.api.item.IGun");
                tacz = true;
            } catch (ClassNotFoundException e) {
                tacz = false;
            }
        }
    }

    public static boolean loaded()
    {
        initTacZ();
        return tacz;
    }
}
