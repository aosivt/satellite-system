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
        primary key (file_id);


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
                     from parquet.`{parquet_place}*.parquet`'::text);

insert into wrk.file (file_id,file_path) values (DEFAULT, '/media/alex/058CFFE45C3C7827/maiskoe/2016/лето/parquet/S2A_MSIL1C_20160604T052652_N0202_R105_T45UVA_20160604T052842_SAFE/'::text);
insert into wrk.file (file_id,file_path) values (DEFAULT, '/media/alex/058CFFE45C3C7827/maiskoe/2016/лето/parquet/S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE/'::text);
insert into wrk.file (file_id,file_path) values (DEFAULT, '/media/alex/058CFFE45C3C7827/maiskoe/2017/лето/parquet/S2A_MSIL1C_20170527T051651_N0205_R062_T45UVA_20170527T051809_SAFE/'::text);
insert into wrk.file (file_id,file_path) values (DEFAULT, '/media/alex/058CFFE45C3C7827/maiskoe/2017/лето/parquet/S2A_MSIL1C_20170616T051651_N0205_R062_T45UVA_20170616T051835_SAFE/'::text);
insert into wrk.file (file_id,file_path) values (DEFAULT, '/media/alex/058CFFE45C3C7827/maiskoe/2017/лето/parquet/S2A_MSIL1C_20170828T052651_N0205_R105_T45UVA_20170828T053055/'::text);

insert into wrk.file (file_id,file_path) values (DEFAULT, '/media/alex/058CFFE45C3C7827/maiskoe/2018/лето/parquet/S2A_MSIL1C_20180711T051651_N0206_R062_T45UVA_20180711T083042_SAFE/'::text);
insert into wrk.file (file_id,file_path) values (DEFAULT, '/media/alex/058CFFE45C3C7827/maiskoe/2018/лето/parquet/S2A_MSIL1C_20180810T051651_N0206_R062_T45UVA_20180810T084918_SAFE/'::text);
insert into wrk.file (file_id,file_path) values (DEFAULT, '/media/alex/058CFFE45C3C7827/maiskoe/2018/лето/parquet/S2B_MSIL1C_20180619T052649_N0206_R105_T45UVA_20180619T082046_SAFE/'::text);
insert into wrk.file (file_id,file_path) values (DEFAULT, '/media/alex/058CFFE45C3C7827/maiskoe/2018/лето/parquet/S2B_MSIL1C_20180709T052649_N0206_R105_T45UVA_20180709T082036_SAFE/'::text);
insert into wrk.file (file_id,file_path) values (DEFAULT, '/media/alex/058CFFE45C3C7827/maiskoe/2019/лето/parquet/S2A_MSIL1C_20190507T051651_N0207_R062_T45UVA_20190507T083105_SAFE/'::text);
insert into wrk.file (file_id,file_path) values (DEFAULT, '/media/alex/058CFFE45C3C7827/maiskoe/2019/лето/parquet/S2A_MSIL1C_20190818T052651_N0208_R105_T45UVA_20190818T083140_SAFE/'::text);
insert into wrk.file (file_id,file_path) values (DEFAULT, '/media/alex/058CFFE45C3C7827/maiskoe/2019/лето/parquet/S2B_MSIL1C_20190505T052659_N0207_R105_T45UVA_20190505T082217_SAFE/'::text);
insert into wrk.file (file_id,file_path) values (DEFAULT, '/media/alex/058CFFE45C3C7827/maiskoe/2019/лето/parquet/S2B_MSIL1C_20190813T052649_N0208_R105_T45UVA_20190813T080801_SAFE/'::text);
insert into wrk.file (file_id,file_path) values (DEFAULT, '/media/alex/058CFFE45C3C7827/maiskoe/2020/лето/parquet/S2A_MSIL1C_20200421T051651_N0209_R062_T45UVA_20200421T071339_SAFE/'::text);
insert into wrk.file (file_id,file_path) values (DEFAULT, '/media/alex/058CFFE45C3C7827/maiskoe/2020/лето/parquet/S2A_MSIL1C_20200809T051701_N0209_R062_T45UVA_20200809T072208_SAFE/'::text);
insert into wrk.file (file_id,file_path) values (DEFAULT, '/media/alex/058CFFE45C3C7827/maiskoe/2020/лето/parquet/S2B_MSIL1C_20200419T052639_N0209_R105_T45UVA_20200419T081204_SAFE/'::text);
insert into wrk.file (file_id,file_path) values (DEFAULT, '/media/alex/058CFFE45C3C7827/maiskoe/2020/лето/parquet/S2B_MSIL1C_20200817T052649_N0209_R105_T45UVA_20200817T073431_SAFE/'::text);

insert into dim.satellite_image (file_id, geo_transform, projection, name) values (1,'{399960.0,10.0,0.0,6100020.0,0.0,-10.0}','PROJCS["WGS 84 / UTM zone 45N",GEOGCS["WGS 84",DATUM["WGS_1984",SPHEROID["WGS 84",6378137,298.257223563,AUTHORITY["EPSG","7030"]],AUTHORITY["EPSG","6326"]],PRIMEM["Greenwich",0,AUTHORITY["EPSG","8901"]],UNIT["degree",0.0174532925199433,AUTHORITY["EPSG","9122"]],AUTHORITY["EPSG","4326"]],PROJECTION["Transverse_Mercator"],PARAMETER["latitude_of_origin",0],PARAMETER["central_meridian",87],PARAMETER["scale_factor",0.9996],PARAMETER["false_easting",500000],PARAMETER["false_northing",0],UNIT["metre",1,AUTHORITY["EPSG","9001"]],AXIS["Easting",EAST],AXIS["Northing",NORTH],AUTHORITY["EPSG","32645"]]'::text,'S2A_MSIL1C_20160604T052652_N0202_R105_T45UVA_20160604T052842_SAFE');
insert into dim.satellite_image (file_id, geo_transform, projection, name) values (2,'{399960.0,10.0,0.0,6100020.0,0.0,-10.0}','PROJCS["WGS 84 / UTM zone 45N",GEOGCS["WGS 84",DATUM["WGS_1984",SPHEROID["WGS 84",6378137,298.257223563,AUTHORITY["EPSG","7030"]],AUTHORITY["EPSG","6326"]],PRIMEM["Greenwich",0,AUTHORITY["EPSG","8901"]],UNIT["degree",0.0174532925199433,AUTHORITY["EPSG","9122"]],AUTHORITY["EPSG","4326"]],PROJECTION["Transverse_Mercator"],PARAMETER["latitude_of_origin",0],PARAMETER["central_meridian",87],PARAMETER["scale_factor",0.9996],PARAMETER["false_easting",500000],PARAMETER["false_northing",0],UNIT["metre",1,AUTHORITY["EPSG","9001"]],AXIS["Easting",EAST],AXIS["Northing",NORTH],AUTHORITY["EPSG","32645"]]'::text,'S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE');
