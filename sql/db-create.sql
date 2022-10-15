DROP database IF EXISTS conferences;

CREATE database conferences;

USE conferences;

create table users
(
    id         int auto_increment primary key,
    email      varchar(255) not null,
    first_name varchar(255) not null,
    last_name  varchar(255) not null,
    password   varchar(255) not null,
    role       varchar(255) not null,
    language   varchar(2)   not null
);

create table events
(
    id           int auto_increment primary key,
    title        text       not null,
    description  text       not null,
    place        text       not null,
    date         timestamp  not null,
    moderator_id int        not null,
    hidden       boolean    not null,
    statistics   int        not null,
    language     varchar(2) not null,
    foreign key (moderator_id) references users (id)
);

create table reports
(
    id         int auto_increment primary key,
    topic      text    not null,
    event_id   int     not null,
    creator_id int     not null,
    speaker_id int     not null,
    confirmed  boolean not null,
    foreign key (event_id) references events (id),
    foreign key (creator_id) references users (id),
    foreign key (speaker_id) references users (id)
);

create table participants
(
    user_id  int not null,
    event_id int not null,
    foreign key (user_id) references users (id),
    foreign key (event_id) references events (id),
    unique (user_id, event_id)
);

# Users
insert into users
values (default, 'timereim@gmail.com', 'Rostyslav', 'Yavorskiy',
        'ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413',
        'MODERATOR', 'en');

insert into users
values (default, 'adamjames@gmail.com', 'Adam', 'James',
        'ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413',
        'SPEAKER', 'en');
insert into users
values (default, 'montellwoodard@gmail.com', 'Montell', 'Woodard',
        'ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413',
        'SPEAKER', 'en');
insert into users
values (default, 'tonibullock@gmail.com', 'Toni', 'Bullock',
        'ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413',
        'SPEAKER', 'en');
insert into users
values (default, 'lyndenlarsen@gmail.com', 'Lynden', 'Larsen',
        'ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413',
        'SPEAKER', 'en');
insert into users
values (default, 'caitlinroy@gmail.com', 'Caitlin', 'Roy',
        'ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413',
        'SPEAKER', 'en');

insert into users
values (default, 'andriyloyal@gmail.com', 'Andriy', 'Loyal',
        'ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413',
        'USER', 'en');
insert into users
values (default, 'olegharrison@gmail.com', 'Oleg', 'Harrison',
        'ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413',
        'USER', 'en');
insert into users
values (default, 'barryalien@gmail.com', 'Barry', 'Alien',
        'ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413',
        'USER', 'en');
insert into users
values (default, 'carolinayoder@gmail.com', 'Carolina', 'Yoder',
        'ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413',
        'USER', 'en');
insert into users
values (default, 'hadassahdodd@gmail.com', 'Hadassah', 'Dodd',
        'ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413',
        'USER', 'en');
insert into users
values (default, 'reissvillanueva@gmail.com', 'Reiss', 'Villanueva',
        'ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413',
        'USER', 'en');
insert into users
values (default, 'rhiannansenior@gmail.com', 'Rhiannan', 'Senior',
        'ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413',
        'USER', 'en');
insert into users
values (default, 'raheelflores@gmail.com', 'Raheel', 'Flores',
        'ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413',
        'USER', 'en');
insert into users
values (default, 'garinmelia@gmail.com', 'Garin', 'Melia',
        'ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413',
        'USER', 'en');
insert into users
values (default, 'christianhorton@gmail.com', 'Christian', 'Horton',
        'ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413',
        'USER', 'en');
insert into users
values (default, 'ayubryder@gmail.com', 'Ayub', 'Ryder',
        'ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413',
        'USER', 'en');
insert into users
values (default, 'heidilee@gmail.com', 'Heidi', 'Lee',
        'ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413',
        'USER', 'en');
insert into users
values (default, 'brunolewis@gmail.com', 'Bruno', 'Lewis',
        'ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413',
        'USER', 'en');

insert into users
values (default, 'timereim2@gmail.com', 'Rostyslav', 'Yavorskiy',
        'ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413',
        'MODERATOR', 'en');

# Events
insert into events
values (default, 'The internet is harmful',
        'The Internet is not only a source of information but a medium that connects almost every aspect of our life. The Internet is a place of great ease and infinite connectivity, but also a place of great vulnerability. In a world of the internet, we live through infinitely complex virtual networks, barely able to trace where our information is coming from and going and thus posing a threat not only to our lives but also to the lives of our children. The digital world plays an immense role in the day-to-day activities of 21st-century children. The U.S. National Library of Medicine National Institutes of Health (NIH) reports teens between the ages of 8 and 28 to spend about 44.5 hours a week in front of a digital screen, according to another report 23 per cent of kids have reported that they feel that they are addicted to video games. As the younger generation is growing more and more tech-savvy and dependent on the internet, they are being exposed to the various malicious side of the internet.',
        'Vinnitskaya oblast, Vinnitsa, Voїnіv-Іnternatsіonalіstіv, bld. 8, appt. 67', FROM_UNIXTIME(1662625359), 1,
        false, -1, 'en');
insert into events
values (default, 'All about psychology',
        'Psychology is the scientific study of mind and behavior. Psychology includes the study of conscious and unconscious phenomena, including feelings and thoughts. It is an academic discipline of immense scope, crossing the boundaries between the natural and social sciences. Psychologists seek an understanding of the emergent properties of brains, linking the discipline to neuroscience. As social scientists, psychologists aim to understand the behavior of individuals and groups. is a Greek letter which is commonly associated with the science of psychology.',
        'Klynove, 15, Khmelnytskyi region, 32048, Ukraine', FROM_UNIXTIME(1669021200), 1, false, -1, 'en');
insert into events
values (default, 'Financial literacy',
        'Financial literacy is the ability to understand and effectively use various financial skills, including personal financial management, budgeting, and investing. The meaning of financial literacy is the foundation of your relationship with money, and it is a lifelong journey of learning. The earlier you start, the better off you will be because education is the key to success when it comes to money.',
        'Brovary, 21, Poltava region, 38425, Ukraine', FROM_UNIXTIME(1669039200), 1, false, -1, 'en');
insert into events
values (default, 'What is programming?',
        'We all have heard about Computer Programming gaining a lot of popularity in the past 3 decades. So many students these days want to opt for a Computer Science stream in order to get a job at their dream tech company - Google, Facebook, Microsoft, Apple, and whatnot.',
        'Brovary, 9, Rivne region, 34135, Ukraine', FROM_UNIXTIME(1668520800), 1, false, -1, 'en');
insert into events
values (default, 'Healthy Eating',
        'Eating a healthy diet is not about strict limitations, staying unrealistically thin, or depriving yourself of the foods you love. Rather, it’s about feeling great, having more energy, improving your health, and boosting your mood.',
        'Lviv, 8, Lviv region, 80081, Ukraine', FROM_UNIXTIME(1668706200), 1, false, -1, 'en');
insert into events
values (default, 'Is programming hard?',
        'Programming has a reputation for being one of the most difficult disciplines to master. Considering how different it is from traditional forms of education, including college degrees in computer science, it''s not hard to see why some people have difficulty learning how to code.',
        'Lviv, 15, Vinnytsia region, 22714, Ukraine', FROM_UNIXTIME(1669211100), 1, false, -1, 'en');
insert into events
values (default, 'Global warming',
        'The Earth is warming up, and humans are at least partially to blame. The causes, effects, and complexities of global warming are important to understand so that we can fight for the health of our planet.',
        'Lutsk, 12, Poltava region, 39232, Ukraine', FROM_UNIXTIME(1669122000), 1, false, -1, 'en');
insert into events
values (default, 'A Nuclear War Could Starve Billions, But One Country May Be Safer Than The Rest',
        'It starts with a single mushroom-shaped cloud the world hoped to never see again. Retaliation prompts tit-for-tat attacks, each intended to end this latest War of All Wars, until a week or so later Earth begins to shiver beneath a pall of soot and dust. Scenarios mapping and calculating the devastation of a nuclear winter are nothing new, dating back to a time when the Cold War was nightly news. Decades on, we know a lot more about the finer effects of particulates in the atmosphere on our agriculture. And the sums remain as grim as ever. Using the latest data on crop yields and fisheries resources, a group of scientists from around the globe have proposed six scenarios approximating what we might expect of food supplies in the aftermath of a rapidly escalating nuclear conflict between warring states.',
        'Kalush, 23, Mykolaiv region, 56020, Ukraine', FROM_UNIXTIME(1669294800), 1, false, -1, 'en');
insert into events
values (default, 'Is artificial intelligence a threat to our existence?',
        'AI applications that are in physical contact with humans or integrated into the human body could pose safety risks as they may be poorly designed, misused or hacked. Poorly regulated use of AI in weapons could lead to loss of human control over dangerous weapons.',
        'Chernivtsi, 7, Kyiv region, 7250, Ukraine', FROM_UNIXTIME(1669309200), 1, false, -1, 'en');
insert into events
values (default, 'How harmful is smoking?',
        'Smoking causes cancer, heart disease, stroke, lung diseases, diabetes, and chronic obstructive pulmonary disease (COPD), which includes emphysema and chronic bronchitis. Smoking also increases risk for tuberculosis, certain eye diseases, and problems of the immune system, including rheumatoid arthritis.',
        'Dobromil, 24, Vinnytsia region, 22036, Ukraine', FROM_UNIXTIME(1669379400), 1, false, -1, 'en');
insert into events
values (default, 'How Does Warren Buffett Choose His Stocks?',
        'Investors have long praised Warren Buffett’s ability to pick which stocks to invest in. Lauded for consistently following value investing principles, Buffett has a net worth of $124.3 billion as of April 18, 2022, according to Forbes. He has resisted the temptations associated with investing in the “next big thing,” and has also used his immense wealth for good by contributing to charities. With his uncanny ability to uncover long-term profitable investments, it''s understandable most investors would like to know exactly what Buffett looks for in a stock.',
        'Lviv, 1, Poltava region, 39213, Ukraine', FROM_UNIXTIME(1669464000), 1, false, -1, 'en');
insert into events
values (default, 'Social engineering',
        'Social engineering is the term used for a broad range of malicious activities accomplished through human interactions. It uses psychological manipulation to trick users into making security mistakes or giving away sensitive information.',
        'Lviv, 29, Chernihiv region, 16707, Ukraine', FROM_UNIXTIME(1669550400), 1, false, -1, 'en');

# Events
insert into events
values (default, 'Інтернет шкідливий',
        'Інтернет – це не лише джерело інформації, а й середовище, яке поєднує майже всі аспекти нашого життя. Інтернет – це місце надзвичайної легкості та нескінченного зв’язку, але водночас і місце великої вразливості. У світі Інтернету ми живемо в нескінченно складних віртуальних мережах, ледь здатних відстежити, звідки надходить і прямує наша інформація, і таким чином становлять загрозу не лише нашому життю, а й життю наших дітей. Цифровий світ відіграє величезну роль у повсякденній діяльності дітей 21-го століття. Національна медична бібліотека США Національний інститут здоров’я (NIH) повідомляє, що підлітки віком від 8 до 28 років проводять близько 44,5 годин на тиждень перед цифровим екраном, згідно з іншим звітом, 23 відсотки дітей повідомили, що вони відчувають що вони залежні від відеоігор. Оскільки молоде покоління стає все більш обізнаним у техніці та стає залежним від Інтернету, воно стикається з різними шкідливими сторонами Інтернету.',
        'Вінницька область, Вінниця, Воїнів-Інтернаціоналістів, буд. 8, кв. 67', FROM_UNIXTIME(1662625359), 1,
        false, -1, 'uk');
insert into events
values (default, 'Все про психологію',
        'Психологія - наукове дослідження розуму та поведінки. Психологія включає вивчення свідомих і несвідомих явищ, включаючи почуття і думки. Це академічна дисципліна величезного масштабу, яка перетинає кордони між природничими та суспільними науками. Психологи прагнуть зрозуміти емерджентні властивості мозку, пов’язуючи цю дисципліну з нейронаукою. Як суспільствознавці, психологи прагнуть зрозуміти поведінку окремих людей і груп. це грецька буква, яка зазвичай асоціюється з наукою психологією.',
        'Клинове, 15, Хмельницька область, 32048, Україна', FROM_UNIXTIME(1669021200), 1, false, -1, 'uk');
insert into events
values (default, 'Фінансова грамотність',
        'Фінансова грамотність – це здатність розуміти та ефективно використовувати різноманітні фінансові навички, включаючи управління особистими фінансами, складання бюджету та інвестування. Значення фінансової грамотності є основою ваших стосунків з грошима, і це все життя. Чим раніше ви почнете, тим краще вам буде, тому що освіта є ключем до успіху, коли мова йде про гроші.',
        'Бровари, 21, Полтавська область, 38425, Україна', FROM_UNIXTIME(1669039200), 1, false, -1, 'uk');
insert into events
values (default, 'Що таке програмування?',
        'Ми всі чули про те, що комп’ютерне програмування набуло великої популярності за останні 3 десятиліття. У наш час так багато студентів хочуть обирати програму «Комп’ютерні науки», щоб отримати роботу в технічній компанії своєї мрії — Google, Facebook, Microsoft, Apple тощо.',
        'Бровари, 9, Рівненська область, 34135, Україна', FROM_UNIXTIME(1668520800), 1, false, -1, 'uk');
insert into events
values (default, 'Здорове харчування',
        'Здорова дієта – це не суворі обмеження, збереження нереально худих або позбавлення себе їжі, яку ти любиш. Скоріше, йдеться про чудове самопочуття, більше енергії, покращення здоров’я та підняття настрою.',
        'Львів, 8, Львівська область, 80081, Україна', FROM_UNIXTIME(1668706200), 1, false, -1, 'uk');
insert into events
values (default, 'Чи важко програмувати?',
        'Програмування має репутацію однієї з найважчих дисциплін для освоєння. З огляду на те, наскільки це відрізняється від традиційних форм навчання, включаючи дипломи коледжу з інформатики, неважко зрозуміти, чому деяким людям важко навчитися кодувати.',
        'Львівська, 15, Вінницька область, 22714, Україна', FROM_UNIXTIME(1669211100), 1, false, -1, 'uk');
insert into events
values (default, 'Глобальне потепління',
        'Земля нагрівається, і принаймні частково в цьому винні люди. Причини, наслідки та складність глобального потепління важливо розуміти, щоб ми могли боротися за здоров’я нашої планети.',
        'Луцька, 12, Полтавська область, 39232, Україна', FROM_UNIXTIME(1669122000), 1, false, -1, 'uk');
insert into events
values (default,
        'Ядерна війна може призвести до голоду мільярдів, але одна країна може бути в більшій безпеці, ніж інші',
        'Вона починається з однієї грибоподібної хмаринки, яку світ сподівався більше ніколи не побачити. Відплата спонукає до атак "око за око", кожна з яких має на меті покласти край цій останній війні усіх війн, доки через тиждень або близько того Земля не почне здригатися під шаром сажі і пилу. Сценарії, що відображають і розраховують руйнування ядерної зими, не є чимось новим, вони походять з часів, коли холодна війна була нічною теленовиною. Десятиліття потому ми знаємо набагато більше про тонкий вплив твердих частинок в атмосфері на наше сільське господарство. І суми залишаються такими ж похмурими, як і раніше. Використовуючи останні дані про врожайність сільськогосподарських культур і рибні ресурси, група вчених з усього світу запропонувала шість сценаріїв, які наближено описують те, що ми можемо очікувати від постачання продовольства після швидкої ескалації ядерного конфлікту між воюючими державами.',
        'Калуш, 23, Миколаївська область, 56020, Україна', FROM_UNIXTIME(1669294800), 1, false, -1, 'uk');
insert into events
values (default, 'Чи загрожує штучний інтелект нашому існуванню?',
        'Застосування ШІ, що перебувають у фізичному контакті з людиною або інтегровані в людський організм, можуть становити ризики для безпеки, оскільки вони можуть бути погано розроблені, використані не за призначенням або зламані. Погано врегульоване використання ШІ в озброєннях може призвести до втрати контролю людини над небезпечною зброєю.',
        'Чернівці, 7, Київська область, 7250, Україна', FROM_UNIXTIME(1669309200), 1, false, -1, 'uk');
insert into events
values (default, 'Наскільки шкідливим є куріння?',
        'Куріння викликає рак, хвороби серця, інсульт, хвороби легенів, діабет і хронічні обструктивні захворювання легенів (ХОЗЛ), які включають емфізему і хронічний бронхіт. Куріння також підвищує ризик розвитку туберкульозу, деяких захворювань очей та проблем з імунною системою, включаючи ревматоїдний артрит.',
        'м. Добромиль, вул. Шевченка, 24, Вінницька область, 22036, Україна', FROM_UNIXTIME(1669379400), 1, false, -1,
        'uk');
insert into events
values (default, 'Як Уоррен Баффет вибирає акції?',
        'Інвестори вже давно високо оцінили вміння Уоррена Баффета обирати, в які акції інвестувати. Відомий за послідовне дотримання принципів ціннісного інвестування, Баффет, за даними Forbes, станом на 18 квітня 2022 року має статки у розмірі 124,3 мільярда доларів США. Він встояв перед спокусами, пов''язаними з інвестуванням в "наступну велику справу", а також використовував своє величезне багатство на благо, роблячи внески в благодійні фонди. З його надзвичайною здатністю знаходити довгострокові прибуткові інвестиції, зрозуміло, що більшість інвесторів хотіли б знати, що саме Баффет шукає в акціях.',
        'вул. Львівська, 1, Полтавська область, 39213, Україна', FROM_UNIXTIME(1669464000), 1, false, -1, 'uk');
insert into events
values (default, 'Соціальна інженерія',
        'Соціальна інженерія - це термін, який використовується для позначення широкого спектру зловмисних дій, що здійснюються через людську взаємодію. Вона використовує психологічні маніпуляції для того, щоб обманом змусити користувачів зробити помилки в системі безпеки або розголосити конфіденційну інформацію.',
        'Львівська, 29, Чернігівська область, 16707, Україна', FROM_UNIXTIME(1669550400), 1, false, -1, 'uk');

# Reports
insert into reports
values (default, 'Online Games', 1, 1, 2, true);

insert into reports
values (default, 'History', 4, 2, 2, true);
insert into reports
values (default, 'Modern programming', 4, 3, 3, true);
insert into reports
values (default, 'Methodologies', 4, 4, 4, true);
insert into reports
values (default, 'Programming languages', 4, 1, 5, false);
insert into reports
values (default, 'Programmers', 4, 5, 5, false);

insert into reports
values (default, 'Історія', 16, 2, 2, true);
insert into reports
values (default, 'Сучасне програмування', 16, 3, 3, true);
insert into reports
values (default, 'Методології', 16, 4, 4, true);
insert into reports
values (default, 'Мови програмування', 16, 1, 5, false);
insert into reports
values (default, 'Програмісти', 16, 5, 5, false);

# Participants
insert into participants
values (7, 4);
insert into participants
values (8, 4);
insert into participants
values (9, 4);
insert into participants
values (10, 4);
insert into participants
values (11, 4);
insert into participants
values (12, 4);
insert into participants
values (13, 4);
insert into participants
values (14, 4);
insert into participants
values (15, 4);
insert into participants
values (16, 4);
insert into participants
values (17, 4);
insert into participants
values (18, 4);

insert into participants
values (7, 1);
insert into participants
values (8, 1);
insert into participants
values (9, 1);
insert into participants
values (10, 1);
insert into participants
values (11, 1);
insert into participants
values (12, 1);
insert into participants
values (13, 1);
insert into participants
values (14, 1);
insert into participants
values (15, 1);
insert into participants
values (16, 1);
insert into participants
values (17, 1);
insert into participants
values (18, 1);

insert into participants
values (7, 16);
insert into participants
values (8, 16);
insert into participants
values (9, 16);
insert into participants
values (10, 16);
insert into participants
values (11, 16);
insert into participants
values (12, 16);
insert into participants
values (13, 16);
insert into participants
values (14, 16);
insert into participants
values (15, 16);
insert into participants
values (16, 16);
insert into participants
values (17, 16);
insert into participants
values (18, 16);