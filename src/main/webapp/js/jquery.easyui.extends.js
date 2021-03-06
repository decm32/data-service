$(function() {
	// 拖拽不允许移出浏览器
	var easyuiPanelOnMove = function(left, top) {
		var parentObj = $(this).panel('panel').parent();
		if (left < 0) {
			$(this).window('move', {
				left : 1
			});
		}
		if (top < 0) {
			$(this).window('move', {
				top : 1
			});
		}
		var width = $(this).panel('options').width;
		var height = $(this).panel('options').height;
//		var width = $(window).width();
//		var height = $(window).height();
		var right = left + width;
		var buttom = top + height;
		var parentWidth = parentObj.width();
		var parentHeight = parentObj.height();
		if (left > parentWidth - width) {
			$(this).window('move', {
				"left" : parentWidth - width
			});
		}
		if (top > parentHeight - $(this).parent().height()) {
			$(this).window('move', {
				"top" : parentHeight - $(this).parent().height() - 15
			});
		}
	};
	// 重写拖拽事件
	$.fn.panel.defaults.onMove = easyuiPanelOnMove;
	$.fn.window.defaults.onMove = easyuiPanelOnMove;
	$.fn.dialog.defaults.onMove = easyuiPanelOnMove;
	// 扩展验证规则
	$.extend($.fn.validatebox.defaults.rules, {
		nospace : {
			validator : function(value) {
				return !/\s/g.test(value);
			},
			message : '您填写的值不能包含空白字符!'
		},
		cron : {
			validator : function(value) {
				var strs = value.split(" ");
				var flag = true;
				for (var i in strs)
					if (strs[i] == "")
						flag = false;
				if (!flag)
					return false;
				return /^([*]|[*]\/\d+|[\d,]*|\d+[-]\d+)\s+([*]|[*]\/\d+|[\d,]*|\d+[-]\d+)\s+([*]|[*]\/\d+|[\d,]*|\d+[-]\d+)\s+([*]|[*]\/\d+|[\d,]*|\d+[-]\d+)\s+([*]|[*]\/[0-9]|[0-9,]*|[0-9]+[-][0-9]+)$/gi.test(value);
			},
			message : '格式[ minute(0-59) hour(0-24) day(1-31) month(1-12) week(1-7) ] *表示任意时间!'
		},
		multiEmail : {
			validator : function(value) {
				var values = value.split(",");
				for (var i in values) {
					bol = /^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i.test(values[i])
					if (!bol) {
						return bol;
					}
				}
				return true;
			},
			message : '请填写正确邮箱并且用英文逗号分割!'
		},
		uploadFile : {
			validator : function(value) {
				if (value.toLowerCase().indexOf('excel.right_now_') != 0) {
					return false;
				}
				return true;
			},
			message : '请确保您上传的文件正确!'
		},
		// 一组textbox的唯一值验证
		onlyValue : {
			// 例如一组标签 ：
			// <input seconds_name="key" index="1"> ...
			// <input seconds_name="key" index="2"> ..
			// <input seconds_name="key" index="3"> ..
			// 传入参数就为：index, seconds_name, key
			// 参数一：唯一的索引值的属性名称
			// 参数二：能表示是同一组的属性名称，切记不能用name，easyui生成插件的特殊原因，textbox会分成3个input，因此无法定位到该元素
			// 参数三：参数二的属性值
			validator : function(value, params) {
				var index_name = params[0];
				var ds_index_name = params[1];
				var ds_index_value = params[2];
				var val = $(this).parent().parent().find('[' + ds_index_name + '="' + ds_index_value + '"]').textbox('getValue');
				var index = $(this).parent().parent().find('[' + ds_index_name + '="' + ds_index_value + '"]').attr(index_name);
				var rst = true;
				$('[' + ds_index_name + '="' + ds_index_value + '"]').each(function() {
					if ($(this).attr(index_name) == index) {
						return;
					}
					if (val == $(this).textbox('getValue')) {
						rst = false;
					}
				});
				return rst;
			},
			message : '您填写的值不是唯一的，请检查!'
		}
	});
	// 自定义提示
//	$.dsmessager = {
//		show : function(message) {
//			if (message == undefined || message == null) {
//				message = "";
//			}
//			if (message == "") {
//				return;
//			}
//			return $.messager.show({
//				title : '提示',
//				msg : message,
//				showType : 'fade'
//			});
//		}
//	};
	// 自定义查询
	$.extend({
		DSFindByRoot : function(CONT) {
			return $.find('[ds_index="' + CONT + '"]');
		}
	});
	$.fn.DSAdd = function(CONT) {
		return $(this).attr('ds_index', CONT);
	};
	$.fn.DSFind = function(CONT) {
		return $(this).find('[ds_index="' + CONT + '"]');
	};
	$.fn.DSAll = function() {
		return $(this).find('*[ds_index]');
	};
	$.fn.DSGetIndex = function() {
		return $(this).attr('ds_index');
	};
});
