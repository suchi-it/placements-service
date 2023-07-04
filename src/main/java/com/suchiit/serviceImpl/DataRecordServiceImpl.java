package com.suchiit.serviceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.mongodb.client.result.UpdateResult;
import com.suchiit.constants.StatusConstants;
import com.suchiit.model.CreateDataRecordRequest;
import com.suchiit.model.DataRecord;
import com.suchiit.model.UpdateDataRecordRequest;
import com.suchiit.service.DataRecordService;

import io.micrometer.core.instrument.util.StringUtils;

@Service
public class DataRecordServiceImpl implements DataRecordService {

	@Autowired
	private MongoTemplate mongoTemplate;

	private static final Logger LOGGER = LoggerFactory.getLogger(DataRecordServiceImpl.class);

	public String createDataRecord(CreateDataRecordRequest request) {
		LOGGER.info("DataRecord Creadted Successfully in Implementation");
		Random rand = new Random();

		Criteria criteria = new Criteria();
		criteria.orOperator(
				Criteria.where("email")
						.regex(Pattern.compile(request.getEmail(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("contactNumber").regex(
						Pattern.compile(request.getContactNumber(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)));

		Query query = new Query(criteria);
		DataRecord datarecord = this.mongoTemplate.findOne(query, DataRecord.class);
		if (datarecord == null) {

			DataRecord newdatarecord = new DataRecord();
			// System.out.println("hiiiiii"+newdatarecord);
			BeanUtils.copyProperties(request, newdatarecord);
			newdatarecord.setId(newdatarecord.getFirstName().substring(0, 3)
					+ newdatarecord.getLastName().substring(0, 3) + rand.nextInt(9));
			newdatarecord.setStatus(StatusConstants.PENDING);
			// newdatarecord.setRoles(StatusConstants.DATARECORD);
			newdatarecord.setCreatedAt(new Date(System.currentTimeMillis()));
			// newcandidate.setLastmodifiedDate(new Date(System.currentTimeMillis()));
			this.mongoTemplate.insert(newdatarecord);
			return "DataRecord Successfully created with id: " + newdatarecord.getId();
		} else {
			return "DataRecord Already exists";
		}
	}

	public List<?> getAllDataRecord(String searchInput) {
		LOGGER.info("DataRecord get all records in Implementation");
		Query query = new Query();
		if (StringUtils.isNotEmpty(searchInput)) {
			query = this.getSearchQuery(searchInput);
		}
		List<DataRecord> datarecords = this.mongoTemplate.find(query, DataRecord.class);
		if (!CollectionUtils.isEmpty(datarecords)) {
			return datarecords;
		} else {
			return new ArrayList();
		}
	}

	@Override
	public DataRecord getDataRecordById(String id) {
		LOGGER.info("DataRecord get single records in Implementation");
		Query query = new Query();
		if (StringUtils.isNotEmpty(id)) {
			query.addCriteria(Criteria.where("_id").is(id));
			DataRecord datarecord = this.mongoTemplate.findOne(query, DataRecord.class);
			if (datarecord != null)
				return datarecord;

			else
				return new DataRecord();
		} else
			return new DataRecord();
	}

	public ResponseEntity<?> updateDataRecord(UpdateDataRecordRequest request) {
		LOGGER.info("DataRecord Update records Successfully in Implementation");
		if (StringUtils.isEmpty(request.getCandidateId())) {
			return new ResponseEntity<>("Id canot be Empty ", HttpStatus.BAD_REQUEST);
		}
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(request.getCandidateId()));
		DataRecord datarecord = this.mongoTemplate.findOne(query, DataRecord.class);
		if (datarecord != null) {

			if (StringUtils.isNotEmpty(request.getFirstName())) {
				datarecord.setFirstName(request.getFirstName());
			}
			if (StringUtils.isNotEmpty(request.getMiddleName())) {
				datarecord.setMiddleName(request.getMiddleName());
			}
			if (StringUtils.isNotEmpty(request.getLastName())) {
				datarecord.setLastName(request.getLastName());
			}
			if (StringUtils.isNotEmpty(request.getEmail())) {
				datarecord.setEmail(request.getEmail());
			}
			if (StringUtils.isNotEmpty(request.getContactNumber())) {
				datarecord.setContactNumber(request.getContactNumber());
			}
			if (StringUtils.isNotEmpty(request.getHigherEductaion())) {
				datarecord.setHigherEductaion(request.getHigherEductaion());
			}
			if (StringUtils.isNotEmpty(request.getWorkExperience())) {
				datarecord.setWorkExperience(request.getWorkExperience());
			}
			if (StringUtils.isNotEmpty(request.getTechnology())) {
				datarecord.setTechnology(request.getTechnology());
			}
			if (StringUtils.isNotEmpty(request.getPreferredModeOfWork())) {
				datarecord.setPreferredModeOfWork(request.getPreferredModeOfWork());
			}
			if (StringUtils.isNotEmpty(request.getExpectedsalary())) {
				datarecord.setExpectedsalary(request.getExpectedsalary());
			}
			if (StringUtils.isNotEmpty(request.getWorkAuthorization())) {
				datarecord.setWorkAuthorization(request.getWorkAuthorization());
			}
			if (StringUtils.isNotEmpty(request.getJobSearchlocationpreference())) {
				datarecord.setJobSearchlocationpreference(request.getJobSearchlocationpreference());
			}

			datarecord.setLastmodifiedDate(new Date(System.currentTimeMillis()));
			this.mongoTemplate.save(datarecord);
			return new ResponseEntity<>("DataRecord " + datarecord.getId() + " : is successfully updated",
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>("No DataRecord found with Id- " + request.getCandidateId(),
					HttpStatus.NOT_FOUND);

		}
	}

	public ResponseEntity<?> deleteDataRecord(String id) {
		LOGGER.info("DataRecord Delete successfully in Implementation");
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(id));
		DataRecord datarecord = this.mongoTemplate.findOne(query, DataRecord.class);
		if (datarecord != null) {
			datarecord.setStatus("INACTIVE");
			this.mongoTemplate.save(datarecord);
			return new ResponseEntity<>("DataRecord " + id + ": is successfully deleted", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("No DataRecord found with Id-" + id, HttpStatus.NOT_FOUND);
		}
	}

	public Query getSearchQuery(String searchInput) {
		Query query = new Query();
		List<Criteria> criterias = new LinkedList<>();
		Criteria searchCriteria = new Criteria();
		searchCriteria.orOperator(
				Criteria.where("_id")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("firstName")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("middleName")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("lastName")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("email")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("line")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("address.city")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("address.state")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("address.country")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("address.line")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("address.zipCode")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("phoneNumber")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("higherEducation")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("dueDate")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("role")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("comments")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)));

		criterias.add(searchCriteria);
		if (!CollectionUtils.isEmpty(criterias)) {
			Criteria criteria = new Criteria();
			criteria.andOperator(criterias.stream().toArray(Criteria[]::new));
			query.addCriteria(criteria);
		}
		return query;
	}

	public String approveDataRecord(String id, String status) {
		System.out.println(status);
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(id));
		Update updateDefi = new Update().set("status", status);
		UpdateResult updateFirst = mongoTemplate.updateFirst(query, updateDefi, DataRecord.class);
		System.out.println(updateFirst.getMatchedCount());
		String msg = "";
		if (updateFirst != null) {
			msg = "DataRecord Status Approved :" + id;
		} else {
			msg = "DataRecord Status is Rejected :" + id;
		}
		return msg;
	}

}
