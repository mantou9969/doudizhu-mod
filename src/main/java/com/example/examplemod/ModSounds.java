package com.example.examplemod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import java.util.HashMap;
import java.util.Map;

public class ModSounds {
    // 注册器
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ExampleMod.MODID);
    // 缓存 Map，用于通过名字快速查找音效
    private static final Map<String, RegistryObject<SoundEvent>> SOUND_MAP = new HashMap<>();

    static {
        // ==========================================
        // 1. 基础牌型语音 (101-115, 201-213, 301-313)
        // 这些名字全是数字，本身就是符合规则的
        // ==========================================
        // 单张: 101(A) ... 115(大王)
        for (int i = 1; i <= 15; i++) registerSound(String.format("1%02d", i));
        // 对子: 201(对A) ... 213(对K)
        for (int i = 1; i <= 13; i++) registerSound(String.format("2%02d", i));
        // 三张: 301(三A) ... 313(三K)
        for (int i = 1; i <= 13; i++) registerSound(String.format("3%02d", i));

        // ==========================================
        // 2. 特殊牌型 (必须全小写)
        // ==========================================
        registerSound("zhadan");       // 炸弹
        registerSound("wangzha");      // 王炸
        registerSound("shunzi");       // 顺子
        registerSound("liandui");      // 连对
        registerSound("feiji");        // 飞机
        registerSound("sandaiyi");     // 三带一
        registerSound("sandaiyidui");  // 三带一对
        registerSound("sidaier");      // 四带二
        registerSound("sidailiangdui");// 四带两对

        // ==========================================
        // 3. 交互语音 (必须全小写)
        // ==========================================
        registerSound("baojing1");     // 报警1
        registerSound("baojing2");     // 报警2
        registerSound("order");        // 叫地主
        registerSound("noorder");      // 不叫

        // “不要”和“压死”
        registerSound("buyao1");
        registerSound("buyao2");
        registerSound("buyao3");
        registerSound("buyao4");
        registerSound("dani1");
        registerSound("dani2");
        registerSound("dani3");

        // ==========================================
        // 4. ★ 核心修复：聊天语音 (全部改为小写！)
        // 之前的大写 msgChatMsg 会导致崩溃，现在全部修正
        // ==========================================
        registerSound("msgchatmsg01");
        registerSound("msgchatmsg02");
        registerSound("msgchatmsg03");
        registerSound("msgchatmsg04");
        registerSound("msgchatmsg05");
        registerSound("msgchatmsg06");
        registerSound("msgchatmsg07");
        registerSound("msgchatmsg08");
        registerSound("msgchatmsg09");
        registerSound("msgchatmsg10");
        registerSound("msgchatmsg11");
        registerSound("msgchatmsg12");

        // ==========================================
        // 5. 胜负音效
        // ==========================================
        registerSound("sfx_win");
        registerSound("sfx_lose");
    }

    // 辅助注册方法
    private static RegistryObject<SoundEvent> registerSound(String name) {
        // 创建 ResourceLocation 时，name 会被用作路径，必须小写
        RegistryObject<SoundEvent> sound = SOUNDS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ExampleMod.MODID, name)));
        // 存入 Map 方便查找
        SOUND_MAP.put(name, sound);
        return sound;
    }

    // 获取音效的方法
    public static SoundEvent getSound(String name) {
        if (SOUND_MAP.containsKey(name)) {
            return SOUND_MAP.get(name).get();
        }
        return null;
    }

    // 给主类调用的注册入口
    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}