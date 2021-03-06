package org.blazer.scheduler.core;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.blazer.dataservice.util.IntegerUtil;
import org.blazer.scheduler.expression.CronCalcTimeoutException;
import org.blazer.scheduler.expression.CronException;
import org.blazer.scheduler.util.DateUtil;

/**
 * cron表达式解释
 * 
 * * * * * *
 * 
 * [0-59] [0-23] [1-31] [1-12] [0-7]
 * 
 * - - - - -
 * 
 * | | | | |
 * 
 * | | | | +-- day of week (0 - 7) (Sunday=0 or 7)
 * 
 * | | | +---- month (1 - 12)
 * 
 * | | +------ day of month (1 - 31)
 * 
 * | +-------- hour (0 - 23)
 * 
 * +---------- minute (0 - 59)
 * 
 * 每一位支持的语法：[*]或者[* / 2]或者[1,2,3]或者[1-3]或者[1]
 * 
 * 现在只对语法进行了简单的正则校验，并不完善，待后续完善。
 * 
 */
public class CronParserHelper {

	// *
	public static final String R1 = "[*]";
	// */2
	public static final String R2 = "[*]/\\d+";
	// 1,2,3 或者 2
	public static final String R3 = "[\\d,]*";
	// 2-5
	public static final String R4 = "\\d+[-]\\d+";

	// public static final String ONE = "([*]|[*]/\\d+|[\\d,]*)";
	public static final String ONE = "(" + R2 + "|" + R4 + "|" + R1 + "|" + R3 + ")";

	public static final String BLANK = "\\s+";

	// Minute Hour Day Month Weekday
	public static final String EXPRESSION = ONE + BLANK + ONE + BLANK + ONE + BLANK + ONE + BLANK + ONE;

	public static final Pattern pattern = Pattern.compile(EXPRESSION);

	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * 根据cron表达式，获得现在这个时间点最近一次的时间
	 * 
	 * @param cron
	 *            表达式
	 * @return
	 * @throws CronException
	 * @throws CronCalcTimeoutException
	 */
	public static Date getNextDate(String cron) throws CronException, CronCalcTimeoutException {
		return getNextDate(DateUtil.newDate(), cron);
	}

	/**
	 * 根据date时间和cron表达式，获得现在这个时间点最近一次的时间
	 * 
	 * @param date
	 *            时间参数
	 * @param cron
	 *            表达式
	 * 
	 * @return
	 * @throws CronException
	 * @throws CronCalcTimeoutException
	 */
	public static Date getNextDate(Date date, String cron) throws CronException, CronCalcTimeoutException {
		if (cron == null) {
			throw new CronException("not valid cron [" + cron + "]");
		}
		if (!isValid(cron)) {
			throw new CronException("not valid cron [" + cron + "]");
		}
		// 将cron表达式转换成string数组
		String[] array = toArray(cron);
		String minute = array[0];
		String hour = array[1];
		String day = array[2];
		String month = array[3];
		String weekday = array[4];
		Date _date = new Date(date.getTime());

		Calendar c = Calendar.getInstance();
		c.setTime(_date);
		// 由于是NextDate，Next表示下一分钟，步长是1分钟
		c.add(Calendar.MINUTE, 1);
		_date = c.getTime();

		int calcCount = 0;
		// 默认如果大于n年的话算计算出错
		int calcDays = 365 * 12;
		// 循环获得下一个时间
		while (true) {
			int days = (int) ((_date.getTime() - date.getTime()) / (1000 * 3600 * 24));
			if (days >= calcDays) {
				throw new CronCalcTimeoutException(
						"cron [" + cron + "] calc days " + days + "[" + sdf.format(_date) + "] beyond " + calcDays + ", please check the cron.");
			}
			calcCount++;

			// 验证是否属于该周，如果不属于，则进入下一个循环
			if (!checkWeek(c, weekday)) {
				// TODO : 此处为了提高效率，当不符合该周几时候，强制下一次计算为下一天 yyyy-MM-dd(Next Day)
				// 00:00:xx
				c.add(Calendar.DAY_OF_YEAR, 1);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.HOUR_OF_DAY, 0);
				_date = c.getTime();
				continue;
			}

			// 验证是否属于该月，如果不属于，则进入下一个循环
			if (!checkMonth(c, month)) {
				// TODO : 此处为了提高效率，当不符合该月时候，强制下一次计算为下一月 yyyy-MM(Next Month)-01
				// 00:00:xx
				c.add(Calendar.MONTH, 1);
				c.set(Calendar.DAY_OF_MONTH, 1);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.HOUR_OF_DAY, 0);
				_date = c.getTime();
				continue;
			}

			// 验证是否属于该天，如果不属于，则进入下一个循环
			if (!checkDay(c, day)) {
				// TODO : 此处为了提高效率，当不符合该天时候，强制下一次计算为下一天 yyyy-MM-dd(Next Day)
				// 00:00:xx
				c.add(Calendar.DAY_OF_YEAR, 1);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.HOUR_OF_DAY, 0);
				_date = c.getTime();
				continue;
			}

			// 验证是否属于该小时，如果不属于，则进入下一个循环
			if (!checkHour(c, hour)) {
				// TODO : 此处为了提高效率，当不符合该小时时候，强制下一次计算为下一小时 yyyy-MM-dd HH(Next
				// Hour):00:xx
				c.add(Calendar.HOUR_OF_DAY, 1);
				c.set(Calendar.MINUTE, 0);
				_date = c.getTime();
				continue;
			}

			// 验证是否属于该分钟，如果不属于，则进入下一个循环
			if (!checkMinute(c, minute)) {
				// TODO : 此处为了提高效率，当不符合该分钟的时候，强制下一次计算为下一分钟 yyyy-MM-dd HH:mm(Next
				// Minute):xx
				c.add(Calendar.MINUTE, 1);
				_date = c.getTime();
				continue;
			}

			// 验证全部通过，结束
			break;
		}
		System.out.println("calc count total : " + calcCount);
		return _date;
	}

	/**
	 * 根据正则表达式，切分* * * * *获得数组
	 * 
	 * @param expression
	 * @return
	 */
	private static String[] toArray(String expression) {
		Matcher m = pattern.matcher(expression);
		String[] arr = new String[m.groupCount()];
		if (m.find()) {
			for (int i = 0; i < arr.length; i++) {
				arr[i] = m.group(i + 1);
			}
		}
		return arr;
	}

	/**
	 * 验证是否符合该分钟 minute (0 - 59)
	 */
	public static boolean checkMinute(Calendar c, String minute) {
		if (minute.matches(R1)) {
			// do nothing
		}
		if (minute.matches(R2)) {
			String[] strs = minute.split("/");
			Integer step = IntegerUtil.getInt0(strs[1]);
			// 当前分钟
			int currentMinute = c.get(Calendar.MINUTE);
			// 是否符合当前分钟
			if (currentMinute % step != 0) {
				return false;
			}
		}
		if (minute.matches(R3)) {
			String[] minutes = minute.split(",");
			// 当前分钟
			int currentMinute = c.get(Calendar.MINUTE);
			// 是否符合当前分钟
			boolean flag = false;
			for (String s : minutes) {
				if (IntegerUtil.getInt0(s) == currentMinute) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				return false;
			}
		}
		if (minute.matches(R4)) {
			String[] minutes = minute.split("-");
			// 当前分钟
			int currentMinute = c.get(Calendar.MINUTE);
			// 是否符合当前分钟
			boolean flag = false;
			int begin = IntegerUtil.getInt0(minutes[0]);
			int end = IntegerUtil.getInt0(minutes[1]);
			for (int i = begin; i <= end; i++) {
				if (i == currentMinute) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 验证是否符合该小时 hour (0 - 23)
	 */
	public static boolean checkHour(Calendar c, String hour) {
		if (hour.matches(R1)) {
			// do nothing
		}
		if (hour.matches(R2)) {
			String[] strs = hour.split("/");
			Integer step = IntegerUtil.getInt0(strs[1]);
			// 当前小时
			int currentHour = c.get(Calendar.HOUR_OF_DAY);
			// 是否符合当前小时
			if (currentHour % step != 0) {
				return false;
			}
		}
		if (hour.matches(R3)) {
			String[] hours = hour.split(",");
			// 当前小时
			int currentHour = c.get(Calendar.HOUR_OF_DAY);
			// 是否符合当前小时
			boolean flag = false;
			for (String s : hours) {
				if (IntegerUtil.getInt0(s) == currentHour) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				return false;
			}
		}
		if (hour.matches(R4)) {
			String[] hours = hour.split("-");
			// 当前小时
			int currentHour = c.get(Calendar.HOUR_OF_DAY);
			// 是否符合当前小时
			boolean flag = false;
			int begin = IntegerUtil.getInt0(hours[0]);
			int end = IntegerUtil.getInt0(hours[1]);
			for (int i = begin; i <= end; i++) {
				if (i == currentHour) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 验证是否符合该天 day of month (1 - 31)
	 */
	public static boolean checkDay(Calendar c, String day) {
		if (day.matches(R1)) {
			// do nothing
		}
		if (day.matches(R2)) {
			String[] strs = day.split("/");
			Integer step = IntegerUtil.getInt0(strs[1]);
			// 当前第几天
			int currentDay = c.get(Calendar.DAY_OF_MONTH);
			// 是否符合当前天数
			if (currentDay % step != 0) {
				return false;
			}
		}
		if (day.matches(R3)) {
			String[] days = day.split(",");
			// 当前第几天
			int currentDay = c.get(Calendar.DAY_OF_MONTH);
			// 是否符合当前天数
			boolean flag = false;
			for (String s : days) {
				if (IntegerUtil.getInt0(s) == currentDay) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				return false;
			}
		}
		if (day.matches(R4)) {
			String[] days = day.split("-");
			// 当前第几天
			int currentDay = c.get(Calendar.DAY_OF_MONTH);
			// 是否符合当前天数
			boolean flag = false;
			int begin = IntegerUtil.getInt0(days[0]);
			int end = IntegerUtil.getInt0(days[1]);
			for (int i = begin; i <= end; i++) {
				if (i == currentDay) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 验证是否符合该月 month (1 - 12)
	 */
	public static boolean checkMonth(Calendar c, String month) {
		if (month.matches(R1)) {
			// do nothing
		}
		if (month.matches(R2)) {
			String[] strs = month.split("/");
			Integer step = IntegerUtil.getInt0(strs[1]);
			// 当前第几月
			int currentMonth = c.get(Calendar.MONTH) + 1;
			// 是否符合当前月
			if (currentMonth % step != 0) {
				return false;
			}
		}
		if (month.matches(R3)) {
			String[] months = month.split(",");
			// 当前第几月
			int currentMonth = c.get(Calendar.MONTH) + 1;
			// 是否符合当前月
			boolean flag = false;
			for (String s : months) {
				if (IntegerUtil.getInt0(s) == currentMonth) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				return false;
			}
		}
		if (month.matches(R4)) {
			String[] months = month.split("-");
			// 当前第几月
			int currentMonth = c.get(Calendar.MONTH) + 1;
			// 是否符合当前月
			boolean flag = false;
			int begin = IntegerUtil.getInt0(months[0]);
			int end = IntegerUtil.getInt0(months[1]);
			for (int i = begin; i <= end; i++) {
				if (i == currentMonth) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 验证是否符合该周
	 */
	public static boolean checkWeek(Calendar c, String weekday) {
		if (weekday.matches(R1)) {
			// do nothing
		}
		if (weekday.matches(R2)) {
			String[] strs = weekday.split("/");
			Integer step = IntegerUtil.getInt0(strs[1]);
			// 当前星期几
			int currentWeek = DateUtil.getRealWeek(c.get(Calendar.DAY_OF_WEEK));
			// 是否符合当前星期
			if (currentWeek % step != 0) {
				return false;
			}
		}
		if (weekday.matches(R3)) {
			String[] weeks = weekday.split(",");
			// 当前星期几
			int currentWeek = c.get(Calendar.DAY_OF_WEEK);
			// 是否符合当前星期
			boolean flag = false;
			for (String s : weeks) {
				if (DateUtil.realWeekEqualsWeek(IntegerUtil.getInt0(s), currentWeek)) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				return false;
			}
		}
		if (weekday.matches(R4)) {
			String[] weeks = weekday.split("-");
			// 当前星期几
			int currentWeek = c.get(Calendar.DAY_OF_WEEK);
			// 是否符合当前星期
			boolean flag = false;
			int begin = IntegerUtil.getInt0(weeks[0]);
			int end = IntegerUtil.getInt0(weeks[1]);
			for (int i = begin; i <= end; i++) {
				if (DateUtil.realWeekEqualsWeek(i, currentWeek)) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 验证是否是一个正确的cron表达式
	 * 
	 * @param cron
	 * @return
	 */
	public static boolean isValid(String cron) {
		if (cron == null)
			return false;
		cron = cron.trim();
		return cron.matches(EXPRESSION);
	}

	/**
	 * 验证是否是一个不正确的cron表达式
	 * 
	 * @param cron
	 * @return
	 */
	public static boolean isNotValid(String cron) {
		if (cron == null)
			return true;
		cron = cron.trim();
		return !cron.matches(EXPRESSION);
	}

}
