INSERT INTO `dict2go`.`translation_invalid`
(
`src_key`,
`src_lng`,
`tgt_lng`,
`src_val`,
`tgt_val`,
`src_gen`,
`src_cat`,
`src_typ`,
`src_use`)
SELECT
`translation`.`src_key`,
`translation`.`src_lng`,
`translation`.`tgt_lng`,
`translation`.`src_val`,
`translation`.`tgt_val`,
`translation`.`src_gen`,
`translation`.`src_cat`,
`translation`.`src_typ`,
`translation`.`src_use`
FROM `dict2go`.`translation` WHERE src_lng > 200 OR tgt_lng > 200;



select src_lng, count(trl_id) as num from translation group by src_lng order by num desc;

select src_lng, count(trl_id) as num from dict2go.translation_invalid group by src_lng order by num desc;


select * from `dict2go`.`translation` WHERE src_lng > 200 limit 5000;
select * from `dict2go`.`translation` WHERE tgt_lng > 200 limit 5000;






INSERT INTO `dict2go`.`translation`
(
`src_key`,
`src_lng`,
`tgt_lng`,
`src_val`,
`tgt_val`,
`src_gen`,
`src_cat`,
`src_typ`,
`src_use`)
SELECT
`src_key`,
201,
`tgt_lng`,
`src_val`,
`tgt_val`,
`src_gen`,
`src_cat`,
`src_typ`,
`src_use`
FROM `dict2go`.`translation_invalid` WHERE src_lng = 457;






