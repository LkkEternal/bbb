package com.chanle.app.data.database

object PinyinConverter {
    private val pinYinMap = mapOf(
        '阿' to "a", '啊' to "a", '锕' to "a",
        '芭' to "ba", '八' to "ba", '巴' to "ba", '叭' to "ba", '拔' to "ba", '跋' to "ba", '靶' to "ba",
        '擦' to "ca", '拆' to "cai", '才' to "cai", '材' to "cai", '财' to "cai",
        '搭' to "da", '达' to "da", '打' to "da", '大' to "da",
        '鹅' to "e", '额' to "e", '恶' to "e",
        '发' to "fa", '法' to "fa", '番' to "fan", '翻' to "fan", '饭' to "fan", '烦' to "fan",
        '嘎' to "ga", '噶' to "ga", '噶' to "ga",
        '哈' to "ha", '蛤' to "ha",
        '击' to "ji", '机' to "ji", '鸡' to "ji", '鸡' to "ji", '积' to "ji", '基' to "ji", '激' to "ji",
        '咖' to "ka", '喀' to "ka",
        '拉' to "la", '辣' to "la", '腊' to "la",
        '妈' to "ma", '麻' to "ma", '马' to "ma", '吗' to "ma",
        '拿' to "na", '哪' to "na", '娜' to "na",
        '哦' to "o", '噢' to "o",
        '啪' to "pa", '趴' to "pa", '爬' to "pa",
        '七' to "qi", '期' to "qi", '其' to "qi", '奇' to "qi", '齐' to "qi", '旗' to "qi",
        '然' to "ran", '燃' to "ran", '染' to "ran",
        '撒' to "sa", '洒' to "sa", '萨' to "sa",
        '他' to "ta", '她' to "ta", '它' to "ta",
        '娃' to "wa", '挖' to "wa", '瓦' to "wa", '蛙' to "wa",
        '西' to "xi", '希' to "xi", '息' to "xi", '悉' to "xi", '稀' to "xi",
        '压' to "ya", '呀' to "ya", '鸭' to "ya", '牙' to "ya", '芽' to "ya", '崖' to "ya",
        '匝' to "za", '杂' to "za", '砸' to "za",
        '白' to "bai", '百' to "bai", '摆' to "bai",
        '菜' to "cai", '彩' to "cai",
        '蛋' to "dan", '淡' to "dan", '蛋' to "dan",
        '肥' to "fei", '费' to "fei",
        '干' to "gan", '赶' to "gan", '感' to "gan",
        '好' to "hao", '号' to "hao",
        '酱' to "jiang", '姜' to "jiang",
        '烤' to "kao", '靠' to "kao",
        '老' to "lao", '捞' to "lao",
        '毛' to "mao", '冒' to "mao", '猫' to "mao",
        '脑' to "nao", '闹' to "nao",
        '泡' to "pao", '跑' to "pao",
        '肉' to "rou", '揉' to "rou",
        '扫' to "sao", '骚' to "sao",
        '糖' to "tang", '汤' to "tang",
        '虾' to "xia", '瞎' to "xia", '峡' to "xia",
        '腰' to "yao", '摇' to "yao", '咬' to "yao",
        '枣' to "zao", '早' to "zao", '澡' to "zao", '皂' to "zao",
        '北' to "bei", '杯' to "bei", '背' to "bei", '被' to "bei",
        '草' to "cao", '操' to "cao", '糙' to "cao",
        '的' to "de", '得' to "de",
        '饿' to "e", '恶' to "e",
        '非' to "fei", '飞' to "fei", '肥' to "fei",
        '给' to "gei",
        '很' to "hen", '恨' to "hen",
        '鸡' to "ji", '季' to "ji", '继' to "ji",
        '开' to "kai", '揩' to "kai",
        '了' to "le", '乐' to "le",
        '没' to "mei", '美' to "mei",
        '你' to "ni", '拟' to "ni",
        '怕' to "pa", '啪' to "pa",
        '去' to "qu", '曲' to "qu", '区' to "qu",
        '色' to "se", '涩' to "se",
        '土' to "tu", '吐' to "tu", '兔' to "tu",
        '为' to "wei", '未' to "wei", '位' to "wei",
        '下' to "xia", '吓' to "xia",
        '以' to "yi", '已' to "yi", '意' to "yi",
        '再' to "zai", '在' to "zai",
        '得' to "dei",
        '地' to "di", '低' to "di", '底' to "di", '弟' to "di", '帝' to "di",
        '豆' to "dou", '都' to "dou", '斗' to "dou",
        '多' to "duo", '夺' to "duo", '朵' to "duo", '躲' to "duo",
        '个' to "ge", '哥' to "ge", '歌' to "ge", '割' to "ge", '搁' to "ge",
        '河' to "he", '合' to "he", '何' to "he", '和' to "he", '盒' to "he",
        '家' to "jia", '加' to "jia", '夹' to "jia", '佳' to "jia", '嘉' to "jia",
        '可' to "ke", '科' to "ke", '课' to "ke", '克' to "ke",
        '了' to "liao", '疗' to "liao", '辽' to "liao", '聊' to "liao",
        '每' to "mei", '妹' to "mei", '美' to "mei",
        '呢' to "ne",
        '破' to "po", '迫' to "po", '朴' to "po",
        '却' to "que", '确' to "que", '雀' to "que",
        '色' to "shai", '晒' to "shai",
        '特' to "te",
        '我' to "wo", '握' to "wo", '卧' to "wo",
        '学' to "xue", '雪' to "xue", '血' to "xue",
        '也' to "ye", '野' to "ye", '叶' to "ye", '夜' to "ye",
        '这' to "zhe", '着' to "zhao", '者' to "zhe", '折' to "zhe",
        '走' to "zou", '揍' to "zou",
        '的' to "di",
        '红' to "hong", '洪' to "hong", '鸿' to "hong", '虹' to "hong",
        '烧' to "shao", '勺' to "shao", '韶' to "shao",
        '宫' to "gong", '公' to "gong", '功' to "gong", '工' to "gong",
        '保' to "bao", '报' to "bao", '饱' to "bao", '抱' to "bao",
        '鸡' to "ji", '鸡' to "ji", '姬' to "ji", '基' to "ji",
        '丁' to "ding", '叮' to "ding", '盯' to "ding",
        '鱼' to "yu", '鱼' to "yu", '于' to "yu", '余' to "yu",
        '香' to "xiang", '相' to "xiang", '想' to "xiang", '响' to "xiang",
        '麻' to "ma", '妈' to "ma", '马' to "ma", '麻' to "ma",
        '婆' to "po", '破' to "po", '迫' to "po",
        '豆' to "dou", '都' to "dou", '斗' to "dou", '兜' to "dou",
        '腐' to "fu", '扶' to "fu", '福' to "fu",
        '排' to "pai", '牌' to "pai", '派' to "pai",
        '骨' to "gu", '古' to "gu", '故' to "gu", '顾' to "gu",
        '冬' to "dong", '东' to "dong", '懂' to "dong",
        '瓜' to "gua", '刮' to "gua", '挂' to "gua",
        '紫' to "zi", '子' to "zi", '自' to "zi", '字' to "zi",
        '菜' to "cai", '才' to "cai", '材' to "cai",
        '花' to "hua", '华' to "hua", '划' to "hua",
        '玉' to "yu", '育' to "yu", '域' to "yu",
        '米' to "mi", '迷' to "mi", '蜜' to "mi", '密' to "mi",
        '汤' to "tang", '糖' to "tang", '堂' to "tang", '棠' to "tang",
        '酸' to "suan", '算' to "suan", '蒜' to "suan",
        '辣' to "la", '腊' to "la", '蜡' to "la",
        '凉' to "liang", '量' to "liang", '两' to "liang",
        '面' to "mian", '面' to "mian", '棉' to "mian",
        '馒' to "man", '满' to "man", '慢' to "man",
        '头' to "tou", '投' to "tou", '透' to "tou",
        '馒' to "man", '瞒' to "man", '满' to "man",
        '包' to "bao", '抱' to "bao", '饱' to "bao", '报' to "bao",
        '饺' to "jiao", '交' to "jiao", '胶' to "jiao", '郊' to "jiao",
        '子' to "zi", '自' to "zi", '字' to "zi", '紫' to "zi",
        '馒' to "man", '蛮' to "man", '满' to "man",
        '米' to "mi", '迷' to "mi", '蜜' to "mi",
        '饭' to "fan", '翻' to "fan", '烦' to "fan",
        '炒' to "chao", '抄' to "chao", '吵' to "chao", '超' to "chao",
        '蛋' to "dan", '淡' to "dan", '旦' to "dan",
        '扬' to "yang", '羊' to "yang", '阳' to "yang", '洋' to "yang",
        '州' to "zhou", '粥' to "zhou", '周' to "zhou",
        '粥' to "zhou", '州' to "zhou", '周' to "zhou",
        '皮' to "pi", '匹' to "pi", '屁' to "pi",
        '黑' to "hei", '嘿' to "hei",
        '木' to "mu", '目' to "mu", '母' to "mu", '墓' to "mu",
        '耳' to "er", '二' to "er", '而' to "er",
        '葱' to "cong", '从' to "cong", '丛' to "cong",
        '油' to "you", '由' to "you", '游' to "you", '有' to "you",
        '拌' to "ban", '半' to "ban", '办' to "ban",
        '心' to "xin", '新' to "xin", '心' to "xin",
        '银' to "yin", '因' to "yin", '音' to "yin", '引' to "yin",
        '莲' to "lian", '连' to "lian", '联' to "lian", '廉' to "lian",
        '子' to "zi", '枣' to "zao", '早' to "zao", '皂' to "zao",
        '红' to "hong", '豆' to "dou", '洪' to "hong",
        '西' to "xi", '米' to "mi", '西' to "xi",
        '甘' to "gan", '露' to "lu",
        '酒' to "jiu", '九' to "jiu", '久' to "jiu", '救' to "jiu",
        '酿' to "niang", '娘' to "niang",
        '小' to "xiao", '圆' to "yuan", '小' to "xiao",
        '意' to "yi", '大' to "da", '利' to "li",
        '番' to "fan", '茄' to "qie", '番' to "fan",
        '牛' to "niu", '排' to "pai", '牛' to "niu",
        '凯' to "kai", '撒' to "sa", '沙' to "sha",
        '蘑' to "mo", '菇' to "gu", '汤' to "tang",
        '汉' to "han", '堡' to "bao",
        '煎' to "jian", '饼' to "bing",
        '腊' to "la", '肉' to "rou",
        '炸' to "zha", '鸡' to "ji",
        '章' to "zhang", '鱼' to "yu",
        '糖' to "tang", '油' to "you",
        '粑' to "ba",
    )

    fun toPinyin(chars: String): String {
        return chars.map { char ->
            pinYinMap[char] ?: char.toString()
        }.joinToString("")
    }

    fun toPinyinInitials(chars: String): String {
        return chars.mapNotNull { char ->
            pinYinMap[char]?.firstOrNull()
        }.joinToString("")
    }

    fun getMatchQuery(keyword: String): String {
        val normalized = normalize(keyword)
        if (normalized.isBlank()) return ""

        val pinyin = toPinyinInitials(normalized)
        return buildString {
            append(normalized)
            if (pinyin.isNotEmpty() && pinyin != normalized) {
                append(" ")
                append(pinyin)
            }
        }
    }

    private fun normalize(text: String): String {
        return text.trim()
            .lowercase()
            .replace(Regex("[\\s\\u3000]+"), "")
            .replace(Regex("[，,。.、！!？?：:；;（）()\\[\\]【】]"), "")
    }
}