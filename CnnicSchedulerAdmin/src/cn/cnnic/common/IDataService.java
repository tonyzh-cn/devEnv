/**
 * Copyright (c) 2015, giscafer (giscafer@outlook.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.cnnic.common;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

public interface IDataService {
	/*=====保存方法======*/
	public  boolean save(String insertedJson,Class<?> c);
	
	/*=====更新方法======*/
	public boolean update(String insertedJson,Class<?> c);
	public int update(String tableName, UpdateFilter updateFilter);
	
	/*=====查询方法======*/
	
	public List<?> getEntityList(String tableName, QueryFilter queryFilter,Model<?> model);
	
	
	/*=====删除方法======*/
	public int del(String tableName,String whereString);
}
