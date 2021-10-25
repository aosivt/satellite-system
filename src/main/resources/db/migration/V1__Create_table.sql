create table project.project
(
    project_id bigserial,
    name_project varchar(200) not null,
    geo_transform float8[] not null,
    projection text not null,
    file_id bigint not null
);

comment on table project.project is 'project';

create unique index project_file_id_uindex
    on project.project (file_id);

create unique index project_project_id_uindex
    on project.project (project_id);

alter table project.project
    add constraint project_pk
        primary key (project_id);


create table wrk.file
(
    file_id bigserial,
    file_path text not null
);

create unique index file_file_id_uindex
    on wrk.file (file_id);

create unique index file_file_path_uindex
    on wrk.file (file_path);

alter table wrk.file
    add constraint file_pk
        primary key (file_path);


create table wrk.indexes
(
    index_id bigserial,
    name varchar(50) not null,
    script text not null
);

comment on table wrk.indexes is 'place template spark sql place';

create unique index indexes_index_id_uindex
    on wrk.indexes (index_id);

create unique index indexes_name_uindex
    on wrk.indexes (name);

create unique index indexes_script_uindex
    on wrk.indexes (script);

alter table wrk.indexes
    add constraint indexes_pk
        primary key (index_id);

create table dim.satellite_image
(
    satellite_image_id bigserial,
    file_id bigint not null,
    geo_transform float8[] not null,
    projection text not null,
    name varchar(200) not null,
    satellite_id bigint
);

comment on table dim.satellite_image is 'satellite image data place';

create unique index satellite_image_file_id_uindex
    on dim.satellite_image (file_id);

create unique index satellite_image_satellite_image_id_uindex
    on dim.satellite_image (satellite_image_id);

alter table dim.satellite_image
    add constraint satellite_image_pk
        primary key (satellite_image_id);

create table dim.satellite
(
    satellite_id bigserial,
    name varchar(200) not null
);

create unique index satellite_satellite_id_uindex
    on dim.satellite (satellite_id);

alter table dim.satellite
    add constraint satellite_pk
        primary key (satellite_id);

INSERT INTO wrk.indexes (index_id, name, script) VALUES (DEFAULT, 'ndvi'::varchar(50), 'select *, array(
                     case when ((dataNIR[0]-dataRed[0]) / (dataNIR[0]+dataRed[0])) is null then 0 else ((dataNIR[0]-dataRed[0]) / (dataNIR[0]+dataRed[0])) end,
                     case when ((dataNIR[1]-dataRed[1]) / (dataNIR[1]+dataRed[1])) is null then 0 else ((dataNIR[0]-dataRed[1]) / (dataNIR[0]+dataRed[1])) end,
                     case when ((dataNIR[2]-dataRed[2]) / (dataNIR[2]+dataRed[2])) is null then 0 else ((dataNIR[0]-dataRed[2]) / (dataNIR[0]+dataRed[2])) end,
                     case when ((dataNIR[3]-dataRed[3]) / (dataNIR[3]+dataRed[3])) is null then 0 else ((dataNIR[0]-dataRed[3]) / (dataNIR[0]+dataRed[3])) end,
                     case when ((dataNIR[4]-dataRed[4]) / (dataNIR[4]+dataRed[4])) is null then 0 else ((dataNIR[0]-dataRed[4]) / (dataNIR[0]+dataRed[4])) end,
                     case when ((dataNIR[5]-dataRed[5]) / (dataNIR[5]+dataRed[5])) is null then 0 else ((dataNIR[0]-dataRed[5]) / (dataNIR[0]+dataRed[5])) end,
                     case when ((dataNIR[6]-dataRed[6]) / (dataNIR[6]+dataRed[6])) is null then 0 else ((dataNIR[0]-dataRed[6]) / (dataNIR[0]+dataRed[6])) end,
                     case when ((dataNIR[7]-dataRed[7]) / (dataNIR[7]+dataRed[7])) is null then 0 else ((dataNIR[0]-dataRed[7]) / (dataNIR[0]+dataRed[7])) end,
                     case when ((dataNIR[8]-dataRed[8]) / (dataNIR[8]+dataRed[8])) is null then 0 else ((dataNIR[0]-dataRed[8]) / (dataNIR[0]+dataRed[8])) end
                     ) ndvi
                     from parquet.`{parquet_place}/*.parquet`'::text)