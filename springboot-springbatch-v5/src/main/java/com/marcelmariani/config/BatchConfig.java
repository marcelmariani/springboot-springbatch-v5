package com.marcelmariani.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.marcelmariani.entity.People;

@Configuration
public class BatchConfig {
	@Autowired
	@Qualifier("transactionManagerApp")
	private PlatformTransactionManager transactionManager;

	@Bean
	public Job job(Step step, JobRepository jobRepository) {
		return new JobBuilder("job", jobRepository).start(step).incrementer(new RunIdIncrementer()).build();
	}

	@Bean
	public Step step(ItemReader<People> reader, ItemWriter<People> writer, JobRepository jobRepository) {
		return new StepBuilder("step", jobRepository).<People, People>chunk(200, transactionManager).reader(reader)
				.writer(writer).build();
	}

	@Bean
	public ItemReader<People> reader() {
		return new FlatFileItemReaderBuilder<People>().name("reader")
				.resource(new FileSystemResource("files/Peoples.csv")).comments("--").delimited()
				.names("nome", "email", "dataNascimento", "idade", "id").targetType(People.class).build();
	}

	@Bean
	public ItemWriter<People> writer(@Qualifier("appDS") DataSource dataSource) {
		return new JdbcBatchItemWriterBuilder<People>().dataSource(dataSource).sql(
				"INSERT INTO PEOPLE (id, nome, email, data_nascimento, idade) VALUES (:id, :nome, :email, :dataNascimento, :idade)")
				.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>()).build();
	}
}