/**
 * 人员信息逻辑处理js
 */
define(function(require, exports, module) {
	var groupController = require('app/group/index.js');
	var editIndex = undefined;
	function endEditing() {
		if (editIndex == undefined) {
			return true
		}
		if ($('#persondg').datagrid('validateRow', editIndex)) {
			var ed = $('#persondg').datagrid('getEditor', {
				index : editIndex,
				field : 'pid'
			});
			// var productname = $(ed.target).combobox('getText');
			// $('#persondg').datagrid('getRows')[editIndex]['productname'] = productname;
			$('#persondg').datagrid('endEdit', editIndex);
			editIndex = undefined;
			return true;
		} else {
			return false;
		}
	}
	function messageShow(info) {
		$.messager.show({
			title : '提示',
			msg : info,
			timeout : 1000,
			showType : 'fade',
			style : {
				right : '',
				bottom : ''
			}
		});
	}
	;

	exports.editRow = function(index, row) {
		if (editIndex != index) {
			if (endEditing()) {
				if (row.lock == 1) {
					messageShow('记录被锁定，不可编辑！');
					return;
				}
				$('#persondg').datagrid('selectRow', index).datagrid(
						'beginEdit', index);
				editIndex = index;
			} else {
				$('#persondg').datagrid('selectRow', editIndex);
			}
		}
	}
	/**
	 * 新增
	 */
	function append() {
		if (endEditing()) {
			$('#persondg').datagrid('appendRow', {
				name : '新人员'
			});
			editIndex = $('#persondg').datagrid('getRows').length - 1;
			$('#persondg').datagrid('selectRow', editIndex).datagrid(
					'beginEdit', editIndex);
		}
	}
	/**
	 * 保存修改（编辑和新增，对不同的人员可以同时编辑和新增）
	 */
	function accept() {
		var $dg = $('#persondg');
		if (endEditing()) {
			var inserted = $dg.datagrid('getChanges', "inserted");
			// var deleted = $dg.datagrid('getChanges', "deleted");
			var updated = $dg.datagrid('getChanges', "updated");
			var effectRow = new Object();
			if (inserted.length) {
				effectRow["inserted"] = JSON.stringify(inserted);
			}
			// if (deleted.length) {
			//     effectRow["deleted"] = JSON.stringify(deleted);
			// }
			if (updated.length) {
				effectRow["updated"] = JSON.stringify(updated);
			}
			var url = window.hostUrl + "commitPerson";
			$.post(url, effectRow, function(res) {
				if (res) {
					$("#example").datagrid('acceptChanges');
					var personNames = [];
					for (var i = 0; i < inserted.length; i++) {
						personNames.push(inserted[i].name);
					}
					;
					groupController.insertPersonIntoGroup(personNames);
				}
				$dg.datagrid('reload');
			}, "JSON");
		}
	}
	/**
	 * 工具条
	 * @type {Array}
	 */
	exports.toolbar = [ {
		text : '新增',
		iconCls : 'icon-add',
		handler : function() {
			append();
		}
	}, '-', {
		text : '删除',
		iconCls : 'icon-cut',
		handler : function() {
			var lockFlag = false;
			var personNames = [];
			var personIds = [];
			var pdg = $("#persondg");
			var rows = pdg.datagrid('getSelections');
			if (!rows.length) {
				return;
			} else {
				//检查选择的记录是否有被锁定的
				for (var i = 0; i < rows.length; i++) {
					personIds.push(rows[i].pid);
					personNames.push(rows[i].name);
					if (rows[i].lock == 1) {
						lockFlag = true;
					}
				}
				;
				if (lockFlag) {
					messageShow('记录被锁定，无法删除！');
					return;
				}
				//确认删除
				$.messager.confirm('删除提醒', '是否确定删除该人员信息?此操作将同时删除该人员的所有班次！', function(ok) {
					if (ok) {
						//确保待删除的人员已经被移出了分组
						var notGroupedPersonCount=0;
						var queryFilter = {
								selectFields : "groupItem",
								whereString : "groupName='未分组'"
							}
						$.ajax({
							url : window.hostUrl + "queryGroup",
							type : 'POST',
							async : false, //同步
							data : queryFilter,
							dataType : 'json'
						}).done(function(result) {
							if(null!=result&&0!=result.length){
								var notGroupedPersonArr=result[0].groupItem.split("|");
								for(var i in personNames){
									for(var j in notGroupedPersonArr){
										if(personNames[i]==notGroupedPersonArr[j]){
											notGroupedPersonCount++;
											continue;
										}
									}
								}
							}
						}).fail(function() {
							console.log("error");
						});
						if(notGroupedPersonCount!=personNames.length){
							$.messager.show({
								title : '提示',
								msg : '请确保所有待删除人员在未分组列表中！',
								timeout : 2000,
								showType : 'fade',
								style : {
									right : '',
									bottom : ''
								}
							});
						}else{
							//删除人员
							$.post(window.hostUrl + "delPerson", {
								IDs : personIds
							}, function() {
								pdg.datagrid('reload');
								$.messager.show({
									title : '提示',
									msg : '删除成功！',
									timeout : 1000,
									showType : 'fade',
									style : {
										right : '',
										bottom : ''
									}
								});
							});
							//删除分组中的人员
							groupController.delPersonFromGroup(personNames);
						}
					}
				});

			}
		}
	}, '-', {
		text : '保存',
		iconCls : 'icon-save',
		handler : function() {
			accept();
		}
	} ];
	/**
	 * 初始化字典值
	 * @return {Array} json数组
	 */
	exports.initDictData = function(callback) {
		$.ajax(
				{
					url : window.hostUrl + 'queryDict?filter='
							+ encodeURIComponent("性别"),
					async : false,
					type : 'GET',
					dataType : 'json'
				// data: {param1: 'value1'},
				}).done(function(data) {
			callback(data);
		}).fail(function() {
			console.log("error");
		})
	}
	/**
	 * 控制lock数据用户不能删除
	 */
	exports.selectChangeHandler = function(index, row) {
		if (row.lock == 1) {

		} else {

		}
	}
});
