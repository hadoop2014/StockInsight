CREATE EXTERNAL TABLE IF NOT EXISTS ShangHaiIndex (
        ReportDate STRING,
        OpenPrice DOUBLE,
        HighPrice DOUBLE,
        LowPrice DOUBLE,
        ClosePrice DOUBLE,
        Volume DOUBLE,
        AdjClosePrice DOUBLE
       )
       ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
       LOCATION 'hdfs://Master:9000/stockindex/ShangHaiIndex';
