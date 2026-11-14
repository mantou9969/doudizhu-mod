import os
import json

# === 配置区域 ===
MODID = "doudizhu"
suits = {"spade": "黑桃", "heart": "红桃", "club": "梅花", "diamond": "方片"}
ranks = {"3": "3", "4": "4", "5": "5", "6": "6", "7": "7", "8": "8", "9": "9", "10": "10",
         "jack": "J", "queen": "Q", "king": "K", "ace": "A", "two": "2"}
jokers = {"black_joker": "小王", "red_joker": "大王"}

# === 路径配置 ===
# 获取脚本所在的目录
script_dir = os.path.dirname(os.path.abspath(__file__))
# 拼接资源路径
base_path = os.path.join(script_dir, "src/main/resources/assets/doudizhu")
model_path = os.path.join(base_path, "models/item")
lang_path = os.path.join(base_path, "lang")

# 确保文件夹存在
os.makedirs(model_path, exist_ok=True)
os.makedirs(lang_path, exist_ok=True)

# 存储 Java 代码
java_registry_lines = []
java_tab_lines = []
lang_dict = {}

# === 核心逻辑 ===
def process_card(english_name, chinese_name):
    # 1. 生成模型 JSON
    json_content = {
        "parent": "item/generated",
        "textures": {
            "layer0": f"{MODID}:item/{english_name}"
        }
    }
    with open(os.path.join(model_path, f"{english_name}.json"), "w", encoding="utf-8") as f:
        json.dump(json_content, f, indent=2)

    # 2. 准备语言文件
    lang_key = f"item.{MODID}.{english_name}"
    lang_dict[lang_key] = chinese_name

    # 3. 准备 Java 代码
    var_name = english_name.upper()
    java_registry_lines.append(
        f'    public static final RegistryObject<Item> {var_name} = ITEMS.register("{english_name}", () -> new Item(new Item.Properties()));'
    )
    java_tab_lines.append(f'                output.accept({var_name}.get());')

# 循环生成
for suit_en, suit_cn in suits.items():
    for rank_en, rank_cn in ranks.items():
        process_card(f"card_{suit_en}_{rank_en}", f"{suit_cn}{rank_cn}")

for joker_en, joker_cn in jokers.items():
    process_card(joker_en, joker_cn)

# 写入语言文件
final_lang = {"itemGroup.doudizhu_tab": "斗地主", **lang_dict}
with open(os.path.join(lang_path, "zh_cn.json"), "w", encoding="utf-8") as f:
    json.dump(final_lang, f, ensure_ascii=False, indent=2)

# === 【重点】把 Java 代码写入到 java_code.txt ===
output_file = os.path.join(script_dir, "java_code.txt")
with open(output_file, "w", encoding="utf-8") as f:
    f.write("// === 第一部分：粘贴到 ITEMS 下面 ===\n\n")
    f.write("\n".join(java_registry_lines))
    f.write("\n\n// === 第二部分：粘贴到 output.accept 里面 ===\n\n")
    f.write("\n".join(java_tab_lines))

print("搞定！代码已保存到 java_code.txt")
# 这行代码会让黑框停住，直到你按回车
input("按回车键退出...")