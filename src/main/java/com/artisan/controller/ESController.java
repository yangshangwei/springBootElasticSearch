package com.artisan.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.artisan.dto.NovelDTO;
import com.artisan.utils.ESUtil;

@RestController
public class ESController {
	


	@Autowired
	private TransportClient client;

	@GetMapping("/book/novel/{id}")
	public ResponseEntity<Map<String, Object>> getByIdFromES(@PathVariable String id) {

		GetResponse response = this.client.prepareGet("book", "novel", id).get();
		if (!response.isExists()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Map<String, Object>>(response.getSource(), HttpStatus.OK);
	}

	@PostMapping("/book/novel/add")
	public ResponseEntity<String> add(NovelDTO novel) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject()
					.field("title", novel.getTitle())
					.field("author", novel.getAuthor())
					.field("word_count", novel.getWordCount())
					.field("public_date", novel.getPublishDate().getTime())
					.endObject();
			IndexResponse response = this.client.prepareIndex("book", "novel").setSource(builder).get();
			return new ResponseEntity<String>(response.getId(), HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/book/novel/del")
	public ResponseEntity<String> delete(String id ){
		DeleteResponse response = this.client.prepareDelete("book", "novel", id).get();
		return new ResponseEntity<String>(response.getResult().toString(),HttpStatus.OK);
	}

	
	/**
	 * 根据id 修改title
	 * @param id
	 * @param title
	 * @return
	 */
	@PutMapping("/book/novel/update")
	public ResponseEntity<String> update(@RequestParam(name="id")String id ,
			@RequestParam(name="title")	String title){
		
		UpdateRequest updateRequest = new UpdateRequest("book", "novel", id);
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject()
					.field("title",title)
					.endObject();
			updateRequest.doc(builder);
			UpdateResponse response = this.client.update(updateRequest).get();
			return new ResponseEntity<String>(response.getResult().toString(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		} 
		
	}

	
	/**
	 * 综合查询
	 * @param title
	 * @param author
	 * @param gtWordCount
	 * @param ltWordCount
	 * @return
	 */
	@PostMapping("/book/novel/query")
	public ResponseEntity<List<Map<String, Object>>> query(
			@RequestParam(name="title",required=false)String title , 
			@RequestParam(name="author",required=false)String author ,
			@RequestParam(name="gtWordCount",defaultValue="0") Integer gtWordCount ,
			@RequestParam(name="ltWordCount",required=false) Integer ltWordCount  ) {
		
		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
		
	
		if (title !=null) {
			boolQueryBuilder.must(QueryBuilders.matchQuery("title", title));
		}
		if (author !=null) {
			boolQueryBuilder.must(QueryBuilders.matchQuery("author", author));
		}
		
		RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("word_count")
				.from(gtWordCount);
		
		if (ltWordCount != null && ltWordCount > 0) {
			rangeQueryBuilder.to(ltWordCount);
		}
		
		// 关联
		boolQueryBuilder.filter(rangeQueryBuilder);
		
		
		SearchRequestBuilder builder = this.client.prepareSearch("book")
			.setTypes("novel")
			.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			.setQuery(boolQueryBuilder)
			.setFrom(0)
			.setSize(10);
		
		System.out.println("请求JSON数据:\n" + builder);
		
		SearchResponse response = builder.get();
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		for(SearchHit searchHit : response.getHits()) {
			list.add(searchHit.getSource());
		}
		return new ResponseEntity<List<Map<String, Object>>>(list, HttpStatus.OK);
	}
	
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PostMapping("/index/create")
	public ResponseEntity create(String index){
		
		if (ESUtil.createIndex(index)) {
			return new ResponseEntity(Boolean.TRUE,HttpStatus.OK);
		}else {
			return new ResponseEntity(HttpStatus.FOUND);
		}
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PostMapping("/index/delete")
	public ResponseEntity deleteIndex(String index){
		
		if (ESUtil.deleteIndex(index)) {
			return new ResponseEntity(Boolean.TRUE,HttpStatus.OK);
		}else {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
		
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PostMapping("/index/isTypeExist")
	public ResponseEntity isTypeExist(String index,String type){
		
		if (ESUtil.isTypeExist(index,type)) {
			return new ResponseEntity(Boolean.TRUE,HttpStatus.OK);
		}else {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
		
	}
}
