package dev.celestiacraft.libs.utils;

import com.nlf.calendar.Lunar;
import com.nlf.calendar.Solar;

import java.time.LocalDate;
import java.util.Date;

public class FestivalUtils {
	/**
	 * 判断今天是否为指定公历日期
	 *
	 * @param month 月份
	 * @param day   日期
	 * @return
	 */
	public static boolean isToday(int month, int day) {
		LocalDate now = LocalDate.now();

		return now.getMonthValue() == month && now.getDayOfMonth() == day;
	}

	/**
	 * 判断今天是否为指定农历日期
	 *
	 * @param month 月份
	 * @param day   日期
	 * @return
	 */
	public static boolean isLunarToday(int month, int day) {
		Solar solar = Solar.fromDate(new Date());
		Lunar lunar = solar.getLunar();

		return lunar.getMonth() == month && lunar.getDay() == day;
	}

	// 元旦
	public static boolean isNewYear() {
		return isToday(1, 1);
	}

	// 三八妇女节
	public static boolean isWomensDay() {
		return isToday(3, 8);
	}

	// 愚人节
	public static boolean isAprilFoolsDay() {
		return isToday(4, 1);
	}

	// 五一劳动节
	public static boolean isLabourDay() {
		return isToday(5, 1);
	}

	// 六一儿童节
	public static boolean isChildrensDay() {
		return isToday(6, 1);
	}

	// 万圣节
	public static boolean isHalloween() {
		return isToday(10, 31);
	}

	// 平安夜
	public static boolean isChristmasEve() {
		return isToday(12, 24);
	}

	// 圣诞节
	public static boolean isChristmas() {
		return isToday(12, 25);
	}

	/**
	 * 跨年夜
	 */
	public static boolean isNewYearsEve() {
		return isToday(12, 31);
	}

	// 春节
	public static boolean isChineseNewYear() {
		return isLunarToday(1, 1);
	}

	// 元宵节
	public static boolean isLanternFestival() {
		return isLunarToday(1, 15);
	}

	// 端午节
	public static boolean isDragonBoatFestival() {
		return isLunarToday(5, 5);
	}

	// 中秋节
	public static boolean isMidAutumnFestival() {
		return isLunarToday(8, 15);
	}

	// 重阳节
	public static boolean isDoubleNinthFestival() {
		return isLunarToday(9, 9);
	}

	// 腊八节
	public static boolean isLabaFestival() {
		return isLunarToday(12, 8);
	}

	// 除夕
	public static boolean isChineseNewYearsEve() {
		Solar tomorrow = Solar.fromDate(new Date(System.currentTimeMillis() + 86400000L));

		Lunar lunar = tomorrow.getLunar();

		// 明天是正月初一
		return lunar.getMonth() == 1 && lunar.getDay() == 1;
	}

	public static String getFestivalGreeting() {
		if (isChineseNewYear()) {
			return "Wishing You Happiness, Prosperity, and Good Fortune in the New Year!";
		}

		if (isChineseNewYearsEve()) {
			return "May Your Family Be Filled with Warmth, Joy, and Reunion Tonight!";
		}

		if (isLanternFestival()) {
			return "May the Lanterns Light Up Your Happiness and Wishes!";
		}

		if (isDragonBoatFestival()) {
			return "Wishing You Peace, Health, and Good Luck During the Dragon Boat Festival!";
		}

		if (isMidAutumnFestival()) {
			return "May the Full Moon Bring You Reunion, Happiness, and Peace!";
		}

		if (isLabaFestival()) {
			return "Wishing You Warmth, Happiness, and Good Fortune This Laba Festival!";
		}

		if (isDoubleNinthFestival()) {
			return "Wishing You Health, Longevity, and Peace on the Double Ninth Festival!";
		}

		if (isNewYear()) {
			return "Wishing You a Wonderful New Year Filled with Joy and Success!";
		}

		if (isWomensDay()) {
			return "Wishing Every Wonderful Woman a Happy and Inspiring Women's Day!";
		}

		if (isAprilFoolsDay()) {
			return "Hope Your April Fools' Day Is Filled with Laughter and Fun!";
		}

		if (isLabourDay()) {
			return "Wishing You a Relaxing and Rewarding Labour Day!";
		}

		if (isChildrensDay()) {
			return "May Every Child Grow Up Happy, Healthy, and Full of Dreams!";
		}

		if (isHalloween()) {
			return "Wishing You a Spooky, Fun, and Candy-Filled Halloween Night!";
		}

		if (isChristmasEve()) {
			return "May Your Christmas Eve Be Filled with Warmth, Peace, and Love!";
		}

		if (isChristmas()) {
			return "Merry Christmas and Best Wishes for Joy and Happiness!";
		}

		if (isNewYearsEve()) {
			return "May the Coming Year Bring You Happiness, Success, and New Adventures!";
		}

		return null;
	}
}