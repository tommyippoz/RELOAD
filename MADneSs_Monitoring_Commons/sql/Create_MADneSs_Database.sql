# This script initializes an empty database for MADneSs

# Creation of the MADneSs database
drop database if exists madness;
create database madness;
use madness;

# Definition of Tables

# Main Tables

create table dataset (
	dataset_id int primary key auto_increment,
    ds_name varchar(40),
    ds_description varchar(1000),
    ds_source varchar(100));

# Workload Tables

create table workload (
	workload_id int primary key auto_increment, 
    wl_name varchar(100), wl_description varchar(300));
    
create table execution_step (
	execution_step_id int primary key auto_increment, 
    es_name varchar(40), 
    es_description varchar(300));
    
create table workload_step (
	workload_step_id int primary key auto_increment, 
    workload_id int, 		
    execution_step_id int, 
    ws_index int, 
	foreign key (workload_id) references workload(workload_id) on delete cascade, 
	foreign key (execution_step_id) references execution_step(execution_step_id) on delete cascade);

# Run Tables

create table run_type (
	run_type_id int primary key auto_increment, 
    rt_description varchar(50));

create table run (
	run_id int primary key auto_increment, 
    dataset_id int,
    run_type_id int, 
    workload_id int,
	foreign key (dataset_id) references dataset(dataset_id) on delete cascade, 
    foreign key (run_type_id) references run_type(run_type_id) on delete cascade, 
	foreign key (workload_id) references workload(workload_id) on delete cascade);
    
create table workload_step_invocation (
	workload_step_invocation_id int primary key auto_increment, 
    run_id int, 
    workload_step_id int, 
    start_time datetime, 
    end_time datetime, 
    response varchar(50),
	foreign key (workload_step_id) references workload_step(workload_step_id) on delete cascade,
    foreign key (run_id) references run(run_id) on delete cascade);

# Anomaly Tables

create table anomaly_type (
	anomaly_type_id int primary key auto_increment, 
    at_description varchar(30));

create table anomaly (
	anomaly_id int primary key auto_increment, 
    anomaly_type_id int, 
    run_id int, 
    an_description varchar(100), 
    an_time datetime, 
    an_duration int default 2, 
    foreign key (anomaly_type_id) references anomaly_type(anomaly_type_id) on delete cascade,
    foreign key (run_id) references run(run_id) on delete cascade);

# Indicator tables

create table indicator_type (
	indicator_type_id int primary key auto_increment, 
    in_description varchar(50));
    
create table indicator (
	indicator_id int primary key auto_increment, 
    indicator_type_id int, 
    in_tag varchar(50), 
    in_description varchar(200), 
    foreign key (indicator_type_id) references indicator_type(indicator_type_id) on delete cascade);

# DataSeries Tables

create table data_category (
	data_category_id int primary key auto_increment, 
    dc_description varchar(50));

create table dataseries (
	dataseries_id int primary key auto_increment, 
    indicator_id int, 
    data_category_id int,
    foreign key (indicator_id) references indicator(indicator_id) on delete cascade, 
    foreign key (data_category_id) references data_category(data_category_id) on delete cascade);

# Data Tables

create table observation (
	observation_id int primary key auto_increment, 
    run_id int, 
    ob_time datetime, 
    foreign key (run_id) references run(run_id) on delete cascade); 

create table dataseries_item (
	dataseries_item_id int, 
	dataseries_id int,
    observation_id int,
    di_value varchar(50), 
	foreign key (dataseries_id) references dataseries(dataseries_id) on delete cascade, 
	foreign key (observation_id) references observation(observation_id) on delete cascade);

# Stat Tables

create table stat (
	stat_id int primary key auto_increment,
    s_avg double default null,
    s_med double default null,
    s_std double default null,
    s_asp double default null);

create table workload_step_stat (
	workload_step_stat_id int primary key auto_increment, 
    workload_step_id int, 
    workload_step_duration_stat int,
    foreign key (workload_step_id) references workload_step(workload_step_id) on delete cascade,
	foreign key (workload_step_duration_stat) references stat(stat_id) on delete cascade);
    
create table workload_step_dataseries_stat (
	workload_step_stat_id int, 
    dataseries_id int, 
    ws_ds_first int,
    ws_ds_last int,
    ws_ds_all int,
    foreign key (workload_step_stat_id) references workload_step_stat(workload_step_stat_id) on delete cascade, 
    foreign key (dataseries_id) references dataseries(dataseries_id) on delete cascade,
    foreign key (ws_ds_first) references stat(stat_id) on delete cascade,
    foreign key (ws_ds_last) references stat(stat_id) on delete cascade,
    foreign key (ws_ds_all) references stat(stat_id) on delete cascade,
    primary key (workload_step_stat_id, dataseries_id));

# Performance Tables
/*
create table performance_type (performance_type_id int primary key auto_increment, pet_description varchar(50));

create table performance (
	performance_id int primary key auto_increment, 
    run_id int, 
    performance_type_id int, 
    probe_type_id int, 
    ind_number int, 
    perf_time int,
    foreign key (run_id) references run(run_id) on delete cascade,
    foreign key (performance_type_id) references performance_type(performance_type_id) on delete cascade,
    foreign key (probe_type_id) references probe_type(probe_type_id) on delete cascade);
*/
    
# Populate Database

insert into data_category (dc_description) values ('Plain'), ('Difference');

# insert into performance_type (pet_description) values ('ot'), ('pmtt'), ('dat'); 
    
insert into anomaly_type (at_description) values 
	('MEMORY'), ('NET_USAGE'), ('CPU'), ('DISK'), ('DEADLOCK'), ('NET_PERM');
    
insert into run_type (rt_description) values
	('Golden'), ('Faulty'), ('Test');
    
insert into indicator_type (in_description) values
	('CENTOS'), ('UBUNTU'), ('WINDOWS'), ('JVM'), ('MYSQL'), 
    ('SQLSERVER'), ('ORACLE'), ('MONGODB'), ('TOMCAT'), ('JBOSS'), ('UNIX_NETWORK');

# Stored Procedures
