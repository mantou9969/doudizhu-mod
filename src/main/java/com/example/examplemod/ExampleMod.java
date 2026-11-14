package com.example.examplemod;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(ExampleMod.MODID)
public class ExampleMod {
    public static final String MODID = "doudizhu";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);

    // 牌桌方块与菜单
    public static final RegistryObject<Block> CARD_TABLE = BLOCKS.register("card_table", CardTableBlock::new);
    public static final RegistryObject<MenuType<CardTableMenu>> CARD_TABLE_MENU = MENUS.register("card_table_menu", () -> IForgeMenuType.create(CardTableMenu::new));
    public static final RegistryObject<Item> CARD_TABLE_ITEM = ITEMS.register("card_table", () -> new BlockItem(CARD_TABLE.get(), new Item.Properties()));

    // 高帽注册
    public static final RegistryObject<Item> SHAME_HAT = ITEMS.register("shame_hat", ShameHatItem::new);

    // 54 张牌
    public static final RegistryObject<Item> CARD_SPADE_3 = ITEMS.register("card_spade_3", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_SPADE_4 = ITEMS.register("card_spade_4", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_SPADE_5 = ITEMS.register("card_spade_5", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_SPADE_6 = ITEMS.register("card_spade_6", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_SPADE_7 = ITEMS.register("card_spade_7", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_SPADE_8 = ITEMS.register("card_spade_8", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_SPADE_9 = ITEMS.register("card_spade_9", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_SPADE_10 = ITEMS.register("card_spade_10", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_SPADE_JACK = ITEMS.register("card_spade_jack", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_SPADE_QUEEN = ITEMS.register("card_spade_queen", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_SPADE_KING = ITEMS.register("card_spade_king", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_SPADE_ACE = ITEMS.register("card_spade_ace", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_SPADE_TWO = ITEMS.register("card_spade_two", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CARD_HEART_3 = ITEMS.register("card_heart_3", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_HEART_4 = ITEMS.register("card_heart_4", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_HEART_5 = ITEMS.register("card_heart_5", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_HEART_6 = ITEMS.register("card_heart_6", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_HEART_7 = ITEMS.register("card_heart_7", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_HEART_8 = ITEMS.register("card_heart_8", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_HEART_9 = ITEMS.register("card_heart_9", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_HEART_10 = ITEMS.register("card_heart_10", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_HEART_JACK = ITEMS.register("card_heart_jack", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_HEART_QUEEN = ITEMS.register("card_heart_queen", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_HEART_KING = ITEMS.register("card_heart_king", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_HEART_ACE = ITEMS.register("card_heart_ace", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_HEART_TWO = ITEMS.register("card_heart_two", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CARD_CLUB_3 = ITEMS.register("card_club_3", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_CLUB_4 = ITEMS.register("card_club_4", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_CLUB_5 = ITEMS.register("card_club_5", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_CLUB_6 = ITEMS.register("card_club_6", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_CLUB_7 = ITEMS.register("card_club_7", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_CLUB_8 = ITEMS.register("card_club_8", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_CLUB_9 = ITEMS.register("card_club_9", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_CLUB_10 = ITEMS.register("card_club_10", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_CLUB_JACK = ITEMS.register("card_club_jack", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_CLUB_QUEEN = ITEMS.register("card_club_queen", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_CLUB_KING = ITEMS.register("card_club_king", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_CLUB_ACE = ITEMS.register("card_club_ace", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_CLUB_TWO = ITEMS.register("card_club_two", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CARD_DIAMOND_3 = ITEMS.register("card_diamond_3", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_DIAMOND_4 = ITEMS.register("card_diamond_4", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_DIAMOND_5 = ITEMS.register("card_diamond_5", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_DIAMOND_6 = ITEMS.register("card_diamond_6", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_DIAMOND_7 = ITEMS.register("card_diamond_7", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_DIAMOND_8 = ITEMS.register("card_diamond_8", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_DIAMOND_9 = ITEMS.register("card_diamond_9", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_DIAMOND_10 = ITEMS.register("card_diamond_10", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_DIAMOND_JACK = ITEMS.register("card_diamond_jack", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_DIAMOND_QUEEN = ITEMS.register("card_diamond_queen", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_DIAMOND_KING = ITEMS.register("card_diamond_king", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_DIAMOND_ACE = ITEMS.register("card_diamond_ace", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARD_DIAMOND_TWO = ITEMS.register("card_diamond_two", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BLACK_JOKER = ITEMS.register("black_joker", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RED_JOKER = ITEMS.register("red_joker", () -> new Item(new Item.Properties()));

    public static final RegistryObject<CreativeModeTab> DOUDIZHU_TAB = CREATIVE_TABS.register("doudizhu_tab", () -> CreativeModeTab.builder()
            .title(Component.literal("斗地主"))
            .icon(() -> CARD_SPADE_3.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(CARD_TABLE_ITEM.get());
                // ★ 放入高帽
                output.accept(SHAME_HAT.get());

                output.accept(CARD_SPADE_3.get()); output.accept(CARD_SPADE_4.get()); output.accept(CARD_SPADE_5.get());
                output.accept(CARD_SPADE_6.get()); output.accept(CARD_SPADE_7.get()); output.accept(CARD_SPADE_8.get());
                output.accept(CARD_SPADE_9.get()); output.accept(CARD_SPADE_10.get()); output.accept(CARD_SPADE_JACK.get());
                output.accept(CARD_SPADE_QUEEN.get()); output.accept(CARD_SPADE_KING.get()); output.accept(CARD_SPADE_ACE.get()); output.accept(CARD_SPADE_TWO.get());
                output.accept(CARD_HEART_3.get()); output.accept(CARD_HEART_4.get()); output.accept(CARD_HEART_5.get());
                output.accept(CARD_HEART_6.get()); output.accept(CARD_HEART_7.get()); output.accept(CARD_HEART_8.get());
                output.accept(CARD_HEART_9.get()); output.accept(CARD_HEART_10.get()); output.accept(CARD_HEART_JACK.get());
                output.accept(CARD_HEART_QUEEN.get()); output.accept(CARD_HEART_KING.get()); output.accept(CARD_HEART_ACE.get()); output.accept(CARD_HEART_TWO.get());
                output.accept(CARD_CLUB_3.get()); output.accept(CARD_CLUB_4.get()); output.accept(CARD_CLUB_5.get());
                output.accept(CARD_CLUB_6.get()); output.accept(CARD_CLUB_7.get()); output.accept(CARD_CLUB_8.get());
                output.accept(CARD_CLUB_9.get()); output.accept(CARD_CLUB_10.get()); output.accept(CARD_CLUB_JACK.get());
                output.accept(CARD_CLUB_QUEEN.get()); output.accept(CARD_CLUB_KING.get()); output.accept(CARD_CLUB_ACE.get()); output.accept(CARD_CLUB_TWO.get());
                output.accept(CARD_DIAMOND_3.get()); output.accept(CARD_DIAMOND_4.get()); output.accept(CARD_DIAMOND_5.get());
                output.accept(CARD_DIAMOND_6.get()); output.accept(CARD_DIAMOND_7.get()); output.accept(CARD_DIAMOND_8.get());
                output.accept(CARD_DIAMOND_9.get()); output.accept(CARD_DIAMOND_10.get()); output.accept(CARD_DIAMOND_JACK.get());
                output.accept(CARD_DIAMOND_QUEEN.get()); output.accept(CARD_DIAMOND_KING.get()); output.accept(CARD_DIAMOND_ACE.get()); output.accept(CARD_DIAMOND_TWO.get());
                output.accept(BLACK_JOKER.get());
                output.accept(RED_JOKER.get());
            }).build());

    @SuppressWarnings("removal")
    public ExampleMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        MENUS.register(modEventBus);
        ModSounds.register(modEventBus);
        PacketHandler.register();

        // ★★★ 核心修复：这里补上了注册 TickHandler 的代码！ ★★★
        // 之前你的代码里没有这行，所以没有 Buff 惩罚
        MinecraftForge.EVENT_BUS.register(GameTickHandler.class);

        modEventBus.addListener(this::clientSetup);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(CARD_TABLE_MENU.get(), CardTableScreen::new);
        });
    }
}