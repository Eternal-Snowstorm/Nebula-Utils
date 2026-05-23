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
	public static boolean isLunarFestival(int month, int day) {
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
		return isLunarFestival(1, 1);
	}

	// 元宵节
	public static boolean isLanternFestival() {
		return isLunarFestival(1, 15);
	}

	// 端午节
	public static boolean isDragonBoatFestival() {
		return isLunarFestival(5, 5);
	}

	// 中秋节
	public static boolean isMidAutumnFestival() {
		return isLunarFestival(8, 15);
	}

	// 重阳节
	public static boolean isDoubleNinthFestival() {
		return isLunarFestival(9, 9);
	}

	// 腊八节
	public static boolean isLabaFestival() {
		return isLunarFestival(12, 8);
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
			return "Happy Spring Festival!";
		}

		if (isChineseNewYearsEve()) {
			return "Happy Chinese New Year's Eve!";
		}

		if (isLanternFestival()) {
			return "Happy Lantern Festival!";
		}

		if (isDragonBoatFestival()) {
			return "Happy Dragon Boat Festival!";
		}

		if (isMidAutumnFestival()) {
			return "Happy Mid-Autumn Festival!";
		}

		if (isLabaFestival()) {
			return "Happy Laba Festival!";
		}

		if (isDoubleNinthFestival()) {
			return "Happy Double Ninth Festival!";
		}

		if (isNewYear()) {
			return "Happy New Year!";
		}

		if (isWomensDay()) {
			return "Happy Women's Day!";
		}

		if (isAprilFoolsDay()) {
			return "Happy April Fools' Day!";
		}

		if (isLabourDay()) {
			return "Happy Labour Day!";
		}

		if (isChildrensDay()) {
			return "Happy Children's Day!";
		}

		if (isHalloween()) {
			return "Happy Halloween!";
		}

		if (isChristmasEve()) {
			return "Merry Christmas Eve!";
		}

		if (isChristmas()) {
			return "Merry Christmas!";
		}

		if (isNewYearsEve()) {
			return "Happy New Year's Eve!";
		}

		return null;
	}
}