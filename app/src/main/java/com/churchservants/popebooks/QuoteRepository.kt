package com.churchservants.popebooks

import java.util.Calendar

object QuoteRepository {

    val quotes = listOf(
        Quote(
            id = 1,
            text = "إن ضعف العقل عن إدراك شيء، فليس معناه أن هذا الشيء غير موجود.",
            bookId = 1,
            bookName = "كتاب انطلاق الروح",
            pageNumber = 12
        ),
        Quote(
            id = 2,
            text = "ربنا موجود، كل شيء يعمل للخير، مسيرها تنتهي.",
            bookId = 5,
            bookName = "كتاب خبرات في الحياة - الجزء الأول والثاني",
            pageNumber = 1
        ),
        Quote(
            id = 3,
            text = "ليس هناك شيء يهدئ النفس ويملأ القلب بالسلام مثل الثقة بوجود الله وعنايته.",
            bookId = 3,
            bookName = "كتاب الهدوء",
            pageNumber = 8
        ),
        Quote(
            id = 4,
            text = "تدرّب أن تقول يارب في كل مشكلة، وسوف ترى كيف يتدخل الله ويعمل بنفسه.",
            bookId = 15,
            bookName = "كتاب حياة الإيمان",
            pageNumber = 24
        ),
        Quote(
            id = 5,
            text = "الذي يهرب من الضيقة يهرب من الله، لأن الله ينقذ في الضيقات.",
            bookId = 4,
            bookName = "كتاب الحروب الروحية",
            pageNumber = 18
        ),
        Quote(
            id = 6,
            text = "الصوم ليس مجرد امتناع عن الطعام، بل هو ارتفاع بالروح فوق شهوات الجسد.",
            bookId = 7,
            bookName = "كتاب روحانية الصوم",
            pageNumber = 5
        ),
        Quote(
            id = 7,
            text = "إن لم تستطع أن تصلي بقلبك، فصلي بفمك، والحرارة ستأتي بالتدريج.",
            bookId = 2,
            bookName = "كتاب حياة التوبة والنقاوة",
            pageNumber = 32
        ),
        Quote(
            id = 8,
            text = "إن الله لا ينظر إلى مقدار ما نعمله، بل ينظر إلى مقدار المحبة التي نعمل بها.",
            bookId = 9,
            bookName = "كتاب كلمة منفعة - الأربعة أجزاء",
            pageNumber = 45
        ),
        Quote(
            id = 9,
            text = "أيها الأبن المبارك، لا تجعل المشاكل تأخذ منك سلامك الداخلي، فالله أكبر من كل مشكلة.",
            bookId = 17,
            bookName = "كتاب الرجوع إلى الله",
            pageNumber = 10
        ),
        Quote(
            id = 10,
            text = "كن مهتماً بإنقاذ نفسك قبل أن تهتم بإنقاذ العالم.",
            bookId = 22,
            bookName = "كتاب الإنسان الروحي",
            pageNumber = 14
        ),
        Quote(
            id = 11,
            text = "كل ما تفعله للناس سيعود إليك بنفس المقدار والأسلوب.",
            bookId = 24,
            bookName = "كتاب التلمذة",
            pageNumber = 21
        ),
        Quote(
            id = 12,
            text = "الدموع الروحية هي غسيل للروح وتنقيتها من أدناس العالم.",
            bookId = 29,
            bookName = "كتاب الدموع في الحياة الروحية",
            pageNumber = 7
        ),
        Quote(
            id = 13,
            text = "تذكر دائماً أن القديسين لم يكونوا من غير أخطاء، بل كانوا سريعي التوبة.",
            bookId = 2,
            bookName = "كتاب حياة التوبة والنقاوة",
            pageNumber = 19
        ),
        Quote(
            id = 14,
            text = "النفس المتواضعة لا تسقط، لأنها تحت الجميع فلا تجد مكاناً تسقط إليه.",
            bookId = 30,
            bookName = "كتاب الذات (الأنا)",
            pageNumber = 15
        ),
        Quote(
            id = 15,
            text = "الهدوء الداخلي ينبع من قلب يتكل كلياً على الله.",
            bookId = 3,
            bookName = "كتاب الهدوء",
            pageNumber = 25
        )
    )

    fun getQuoteForToday(): Quote {
        return getQuoteForCalendar(Calendar.getInstance())
    }

    fun getQuoteForCalendar(calendar: Calendar): Quote {
        val year = calendar.get(Calendar.YEAR)
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val index = ((year * 365) + dayOfYear) % quotes.size
        return quotes[kotlin.math.abs(index)]
    }

    fun getQuoteById(id: Int): Quote {
        return quotes.find { it.id == id } ?: quotes.first()
    }
}
