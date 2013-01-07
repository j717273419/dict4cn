SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

DROP SCHEMA IF EXISTS `dict2go` ;
CREATE SCHEMA IF NOT EXISTS `dict2go` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;
USE `dict2go` ;

-- -----------------------------------------------------
-- Table `dict2go`.`translation`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `dict2go`.`translation` ;

CREATE  TABLE IF NOT EXISTS `dict2go`.`translation` (
  `trl_id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `src_key` BINARY(16) NOT NULL ,
  `src_lng` INT UNSIGNED NOT NULL ,
  `tgt_lng` INT UNSIGNED NOT NULL ,
  `src_val` VARCHAR(2000) NOT NULL ,
  `tgt_val` VARCHAR(8000) NOT NULL ,
  `src_gen` INT UNSIGNED NULL ,
  `src_cat` INT UNSIGNED NULL ,
  `src_typ` INT UNSIGNED NULL ,
  `src_use` INT UNSIGNED NULL ,
  PRIMARY KEY (`trl_id`) ,
  INDEX `idx_key` (`src_key` ASC) ,
  INDEX `idx_lng` (`src_key` ASC, `src_lng` ASC, `tgt_lng` ASC) )
ENGINE = InnoDB;


CREATE  TABLE IF NOT EXISTS `dict2go`.`translation_invalid` (
  `trl_id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `src_key` BINARY(16) NOT NULL ,
  `src_lng` INT UNSIGNED NOT NULL ,
  `tgt_lng` INT UNSIGNED NOT NULL ,
  `src_val` VARCHAR(2000) NOT NULL ,
  `tgt_val` VARCHAR(8000) NOT NULL ,
  `src_gen` INT UNSIGNED NULL ,
  `src_cat` INT UNSIGNED NULL ,
  `src_typ` INT UNSIGNED NULL ,
  `src_use` INT UNSIGNED NULL ,
  PRIMARY KEY (`trl_id`) ,
  INDEX `idx_invalid_key` (`src_key` ASC) ,
  INDEX `idx_invalid_lng` (`src_key` ASC, `src_lng` ASC, `tgt_lng` ASC) )
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `dict2go`.`example`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `dict2go`.`example` ;

CREATE  TABLE IF NOT EXISTS `dict2go`.`example` (
  `exm_id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `trl_id` INT UNSIGNED NOT NULL ,
  `src_val` VARCHAR(8000) NULL ,
  PRIMARY KEY (`exm_id`) ,
  UNIQUE INDEX `trl_id_UNIQUE` (`trl_id` ASC) ,
  INDEX `fk_trl_id_idx` (`trl_id` ASC) ,
  CONSTRAINT `fk_exm_trl_id`
    FOREIGN KEY (`trl_id` )
    REFERENCES `dict2go`.`translation` (`trl_id` )
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `dict2go`.`synonym`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `dict2go`.`synonym` ;

CREATE  TABLE IF NOT EXISTS `dict2go`.`synonym` (
  `syn_id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `trl_id` INT UNSIGNED NOT NULL ,
  `src_val` VARCHAR(8000) NULL ,
  PRIMARY KEY (`syn_id`) ,
  UNIQUE INDEX `trl_id_UNIQUE` (`trl_id` ASC) ,
  INDEX `fk_trl_id_idx` (`trl_id` ASC) ,
  CONSTRAINT `fk_syn_trl_id`
    FOREIGN KEY (`trl_id` )
    REFERENCES `dict2go`.`translation` (`trl_id` )
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `dict2go`.`related`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `dict2go`.`related` ;

CREATE  TABLE IF NOT EXISTS `dict2go`.`related` (
  `rel_id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `trl_id` INT UNSIGNED NOT NULL ,
  `src_val` VARCHAR(8000) NULL ,
  PRIMARY KEY (`rel_id`) ,
  UNIQUE INDEX `trl_id_UNIQUE` (`trl_id` ASC) ,
  INDEX `fk_trl_id_idx` (`trl_id` ASC) ,
  CONSTRAINT `fk_rel_trl_id`
    FOREIGN KEY (`trl_id` )
    REFERENCES `dict2go`.`translation` (`trl_id` )
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `dict2go`.`description`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `dict2go`.`description` ;

CREATE  TABLE IF NOT EXISTS `dict2go`.`description` (
  `dsc_id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `trl_id` INT UNSIGNED NOT NULL ,
  `src_val` VARCHAR(8000) NULL ,
  PRIMARY KEY (`dsc_id`) ,
  UNIQUE INDEX `trl_id_UNIQUE` (`trl_id` ASC) ,
  INDEX `fk_trl_id_idx` (`trl_id` ASC) ,
  CONSTRAINT `fk_dsc_trl_id`
    FOREIGN KEY (`trl_id` )
    REFERENCES `dict2go`.`translation` (`trl_id` )
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- procedure addTranslation
-- -----------------------------------------------------

USE `dict2go`;
DROP procedure IF EXISTS `dict2go`.`addTranslation`;

DELIMITER $$

CREATE PROCEDURE `dict2go`.`addTranslation` (
 srckey BINARY(16), srclng INT UNSIGNED, tgtlng INT UNSIGNED, srcval VARCHAR(1000),
 tgtval VARCHAR(8000), srcgen INT UNSIGNED, srccat INT UNSIGNED, srctyp INT UNSIGNED,
 srcuse INT UNSIGNED)
  LANGUAGE SQL
  NOT DETERMINISTIC
  CONTAINS SQL
  SQL SECURITY DEFINER
  COMMENT ''
BEGIN
  DECLARE old_trlid INT UNSIGNED DEFAULT 0;
  DECLARE old_tgtval VARCHAR(8000);
  DECLARE msg VARCHAR(10);

  SELECT trl.trl_id, trl.tgt_val INTO old_trlid, old_tgtval
  FROM translation trl
  WHERE trl.src_key = srckey AND trl.src_lng = srclng AND trl.tgt_lng = tgtlng AND
        trl.src_val = srcval AND trl.src_gen = srcgen AND trl.src_cat = srccat AND
        trl.src_typ = srctyp AND trl.src_use = srcuse;

  IF (old_trlid > 0) THEN
    IF (NOT old_tgtval LIKE CONCAT('%', tgtval, '%')) THEN
      IF (tgtval LIKE CONCAT('%', old_tgtval, '%')) THEN
        UPDATE translation SET tgt_val = tgtval WHERE trl_id = old_trlid;
      ELSEIF (tgtval <> old_tgtval) THEN
        UPDATE translation SET tgt_val = CONCAT(old_tgtval, ', ', tgtval) WHERE trl_id = old_trlid;
      END IF;
    END IF;
  ELSE
    INSERT INTO translation (src_key, src_lng, tgt_lng, src_val, tgt_val, src_gen, src_cat, src_typ, src_use)
    VALUES (srckey, srclng, tgtlng, srcval, tgtval, srcgen, srccat, srctyp, srcuse);
  END IF;
END$$


DELIMITER $$

CREATE PROCEDURE `dict2go`.`addTranslationInvalid` (
 srckey BINARY(16), srclng INT UNSIGNED, tgtlng INT UNSIGNED, srcval VARCHAR(1000),
 tgtval VARCHAR(8000), srcgen INT UNSIGNED, srccat INT UNSIGNED, srctyp INT UNSIGNED,
 srcuse INT UNSIGNED)
  LANGUAGE SQL
  NOT DETERMINISTIC
  CONTAINS SQL
  SQL SECURITY DEFINER
  COMMENT ''
BEGIN
  DECLARE old_trlid INT UNSIGNED DEFAULT 0;
  DECLARE old_tgtval VARCHAR(8000);
  DECLARE msg VARCHAR(10);

  SELECT trl.trl_id, trl.tgt_val INTO old_trlid, old_tgtval
  FROM translation_invalid trl
  WHERE trl.src_key = srckey AND trl.src_lng = srclng AND trl.tgt_lng = tgtlng AND
        trl.src_val = srcval AND trl.src_gen = srcgen AND trl.src_cat = srccat AND
        trl.src_typ = srctyp AND trl.src_use = srcuse;

  IF (old_trlid > 0) THEN
    IF (NOT old_tgtval LIKE CONCAT('%', tgtval, '%')) THEN
      IF (tgtval LIKE CONCAT('%', old_tgtval, '%')) THEN
        UPDATE translation_invalid SET tgt_val = tgtval WHERE trl_id = old_trlid;
      ELSEIF (tgtval <> old_tgtval) THEN
        UPDATE translation_invalid SET tgt_val = CONCAT(old_tgtval, ', ', tgtval) WHERE trl_id = old_trlid;
      END IF;
    END IF;
  ELSE
    INSERT INTO translation_invalid (src_key, src_lng, tgt_lng, src_val, tgt_val, src_gen, src_cat, src_typ, src_use)
    VALUES (srckey, srclng, tgtlng, srcval, tgtval, srcgen, srccat, srctyp, srcuse);
  END IF;
END$$




DELIMITER ;

SET SQL_MODE = '';
GRANT USAGE ON *.* TO moderator;
 DROP USER moderator;
SET SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
CREATE USER `moderator` IDENTIFIED BY 'rotaredom';

grant ALL on TABLE `dict2go`.`description` to moderator;
grant ALL on TABLE `dict2go`.`example` to moderator;
grant ALL on TABLE `dict2go`.`related` to moderator;
grant ALL on TABLE `dict2go`.`synonym` to moderator;
grant ALL on TABLE `dict2go`.`translation` to moderator;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
