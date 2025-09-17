-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: localhost
-- Thời gian đã tạo: Th9 17, 2025 lúc 04:43 PM
-- Phiên bản máy phục vụ: 10.4.28-MariaDB
-- Phiên bản PHP: 8.2.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `LibraryManagement`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `BOOKS`
--

CREATE TABLE `BOOKS` (
  `BookID` bigint(20) NOT NULL,
  `ISBN` varchar(16) NOT NULL,
  `Title` varchar(100) NOT NULL,
  `Author` varchar(30) NOT NULL,
  `SubjectCode` smallint(6) NOT NULL,
  `SubjectSeq` int(11) NOT NULL,
  `Description` text DEFAULT NULL,
  `Price` decimal(10,2) DEFAULT NULL,
  `ImageURL` varchar(255) DEFAULT NULL
) ;

--
-- Đang đổ dữ liệu cho bảng `BOOKS`
--

INSERT INTO `BOOKS` (`BookID`, `ISBN`, `Title`, `Author`, `SubjectCode`, `SubjectSeq`, `Description`, `Price`, `ImageURL`) VALUES
(4, '002-0001', 'Máy lọc', 'Hoàng', 2, 1, 'Đẹp', 500000.00, '/Users/nguyenhuynhduc1205/eclipse-workspace/LibraryManagerSystem/src/Resources/4373217_java_logo_logos_icon.png'),
(5, '002-0003', 'Sách 3', 'Duc Nguyenn', 2, 3, '', 9000.00, '/Users/nguyenhuynhduc1205/eclipse-workspace/LibraryManagerSystem/src/Resources/8666542_save_icon.png'),
(6, '001-0001', 'Chủ Nghĩa Tối Giản', 'Chi Nguyễn', 1, 1, '', 100000.00, '/Users/nguyenhuynhduc1205/eclipse-workspace/LibraryManagerSystem/src/Resources/2022-03-30-haravan-book-mockup-1-grey-bg_751cf76c9a874ac2ba3e70937cedb752_master.jpeg');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `BOOK_COPIES`
--

CREATE TABLE `BOOK_COPIES` (
  `CopyID` bigint(20) NOT NULL,
  `BookID` bigint(20) NOT NULL,
  `CopySeq` int(11) NOT NULL,
  `CallNumber` varchar(16) NOT NULL,
  `IsAvailable` tinyint(4) NOT NULL DEFAULT 1,
  `Status` varchar(20) NOT NULL DEFAULT 'AVAILABLE',
  `ShelfLocation` varchar(30) DEFAULT NULL
) ;

--
-- Đang đổ dữ liệu cho bảng `BOOK_COPIES`
--

INSERT INTO `BOOK_COPIES` (`CopyID`, `BookID`, `CopySeq`, `CallNumber`, `IsAvailable`, `Status`, `ShelfLocation`) VALUES
(12, 4, 5, 'MA-HO-005', 1, 'AVAILABLE', NULL),
(13, 4, 6, 'MA-HO-006', 1, 'AVAILABLE', NULL),
(14, 4, 7, 'MA-HO-007', 1, 'AVAILABLE', NULL),
(25, 5, 2, 'SA-DU-002', 1, 'AVAILABLE', NULL),
(26, 5, 3, 'SA-DU-003', 1, 'AVAILABLE', NULL),
(27, 5, 4, 'SA-DU-004', 1, 'AVAILABLE', NULL),
(28, 5, 5, 'SA-DU-005', 1, 'AVAILABLE', NULL),
(29, 5, 6, 'SA-DU-006', 1, 'AVAILABLE', NULL),
(30, 5, 7, 'SA-DU-007', 1, 'AVAILABLE', NULL);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `BORROW_RECORDS`
--

CREATE TABLE `BORROW_RECORDS` (
  `RecordID` bigint(20) NOT NULL,
  `SlipID` bigint(20) NOT NULL,
  `CopyID` bigint(20) NOT NULL,
  `DueDate` date NOT NULL,
  `ReturnDate` date DEFAULT NULL,
  `LateFee` decimal(10,2) NOT NULL DEFAULT 0.00,
  `DepositAmount` decimal(10,2) NOT NULL DEFAULT 0.00,
  `CheckinByUserID` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `BORROW_RECORDS`
--

INSERT INTO `BORROW_RECORDS` (`RecordID`, `SlipID`, `CopyID`, `DueDate`, `ReturnDate`, `LateFee`, `DepositAmount`, `CheckinByUserID`) VALUES
(1, 1, 12, '2025-09-20', '2025-09-15', 0.00, 500000.00, 3),
(2, 2, 12, '2025-09-20', '2025-09-15', 0.00, 500000.00, 3),
(3, 3, 13, '2025-09-20', '2025-09-15', 0.00, 500000.00, 6),
(4, 4, 12, '2025-09-20', '2025-09-15', 0.00, 500000.00, 6);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `EMPLOYEES`
--

CREATE TABLE `EMPLOYEES` (
  `SystemID` bigint(20) NOT NULL,
  `EmployeeID` varchar(32) NOT NULL,
  `Name` varchar(120) NOT NULL,
  `Address` varchar(255) DEFAULT NULL,
  `Phone` varchar(32) DEFAULT NULL,
  `Department` varchar(80) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `EMPLOYEES`
--

INSERT INTO `EMPLOYEES` (`SystemID`, `EmployeeID`, `Name`, `Address`, `Phone`, `Department`) VALUES
(1, '1', 'Đức', 'HCM', '099999999', NULL),
(2, '2', 'Duc 2', 'hcm', '09999999', NULL);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `LIB_SETTINGS`
--

CREATE TABLE `LIB_SETTINGS` (
  `SettingID` bigint(20) NOT NULL,
  `DefaultDueDays` int(11) NOT NULL DEFAULT 5,
  `LateFeePerDay` decimal(6,2) NOT NULL DEFAULT 0.10,
  `MaxBooksPerPatron` int(11) NOT NULL DEFAULT 5,
  `DepositMultiplier` decimal(6,2) NOT NULL DEFAULT 1.00,
  `UpdatedAt` datetime NOT NULL DEFAULT current_timestamp(),
  `UpdatedByUserID` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `LIB_SETTINGS`
--

INSERT INTO `LIB_SETTINGS` (`SettingID`, `DefaultDueDays`, `LateFeePerDay`, `MaxBooksPerPatron`, `DepositMultiplier`, `UpdatedAt`, `UpdatedByUserID`) VALUES
(1, 5, 0.10, 5, 1.00, '2025-09-03 18:59:54', NULL);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `LOAN_SLIPS`
--

CREATE TABLE `LOAN_SLIPS` (
  `SlipID` bigint(20) NOT NULL,
  `PatronSysID` bigint(20) NOT NULL,
  `IssueDate` date NOT NULL,
  `DueDaysUsed` int(11) NOT NULL,
  `LateFeePerDayUsed` decimal(6,2) NOT NULL,
  `DepositMultiplierUsed` decimal(6,2) NOT NULL,
  `CheckoutByUserID` bigint(20) NOT NULL,
  `Status` varchar(12) NOT NULL DEFAULT 'OPEN'
) ;

--
-- Đang đổ dữ liệu cho bảng `LOAN_SLIPS`
--

INSERT INTO `LOAN_SLIPS` (`SlipID`, `PatronSysID`, `IssueDate`, `DueDaysUsed`, `LateFeePerDayUsed`, `DepositMultiplierUsed`, `CheckoutByUserID`, `Status`) VALUES
(1, 2, '2025-09-15', 5, 0.10, 1.00, 3, 'OPEN'),
(2, 2, '2025-09-15', 5, 0.10, 1.00, 3, 'OPEN'),
(3, 3, '2025-09-15', 5, 0.10, 1.00, 6, 'OPEN'),
(4, 3, '2025-09-15', 5, 0.10, 1.00, 6, 'OPEN');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `PATRONS`
--

CREATE TABLE `PATRONS` (
  `PatronSysID` bigint(20) NOT NULL,
  `PatronID` varchar(32) NOT NULL,
  `Name` varchar(120) NOT NULL,
  `Address` varchar(255) DEFAULT NULL,
  `Phone` varchar(32) DEFAULT NULL,
  `Email` varchar(120) DEFAULT NULL,
  `IsActive` tinyint(4) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `PATRONS`
--

INSERT INTO `PATRONS` (`PatronSysID`, `PatronID`, `Name`, `Address`, `Phone`, `Email`, `IsActive`) VALUES
(2, '1', 'Đức Nguyễn', 'HCM', '0999929999', 'huynhducnguyen', 1),
(3, '2', 'Đức 2', 'HCM', '0992999999', 'hoang@gmail.com', 1);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `SUBJECTS`
--

CREATE TABLE `SUBJECTS` (
  `SubjectCode` smallint(6) NOT NULL,
  `SubjectName` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `SUBJECTS`
--

INSERT INTO `SUBJECTS` (`SubjectCode`, `SubjectName`) VALUES
(1, 'Duc'),
(2, 'Phát sóng');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `SYSTEM_USERS`
--

CREATE TABLE `SYSTEM_USERS` (
  `UserID` bigint(20) NOT NULL,
  `Username` varchar(64) NOT NULL,
  `PasswordHash` varchar(72) NOT NULL,
  `Role` varchar(20) NOT NULL,
  `IsActive` tinyint(4) NOT NULL DEFAULT 1,
  `StaffSystemID` bigint(20) DEFAULT NULL,
  `LastLogin` datetime DEFAULT NULL
) ;

--
-- Đang đổ dữ liệu cho bảng `SYSTEM_USERS`
--

INSERT INTO `SYSTEM_USERS` (`UserID`, `Username`, `PasswordHash`, `Role`, `IsActive`, `StaffSystemID`, `LastLogin`) VALUES
(3, 'admin', '$2a$12$gwFNzijZizOWaHDdc3DsYOhz0YTbyFfFqVIPslpKT5FjV/DG3KZGe', 'ADMIN', 1, NULL, '2025-09-15 19:16:14'),
(4, 'lib1', '$2a$12$tXLM4yVBouw.JHdprJfoCeLU2fMIqNCJyD/sDcDsqqi1sDGdUH1ju', 'LIBRARIAN', 1, 1, '2025-09-09 23:44:55'),
(5, 'lib2', '$2a$12$G5uV5eAzPbAxHh6CcWR8TuR2x0UHJjLPObXo56qREQqwpceGUKMv.', 'LIBRARIAN', 1, 2, '2025-09-09 23:28:46'),
(6, '1', '$2a$12$9ZrpWzFqOvTWogvQINYuJeL5HZZYmDh.dg8aiuNYaoyPPF/QPylXO', 'LIBRARIAN', 1, 1, '2025-09-15 19:16:28');

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `BOOKS`
--
ALTER TABLE `BOOKS`
  ADD PRIMARY KEY (`BookID`),
  ADD UNIQUE KEY `uk_isbn` (`ISBN`),
  ADD UNIQUE KEY `uk_subject_seq` (`SubjectCode`,`SubjectSeq`),
  ADD UNIQUE KEY `uk_title_author` (`Title`,`Author`),
  ADD KEY `idx_books_title` (`Title`),
  ADD KEY `idx_books_author` (`Author`);

--
-- Chỉ mục cho bảng `BOOK_COPIES`
--
ALTER TABLE `BOOK_COPIES`
  ADD PRIMARY KEY (`CopyID`),
  ADD UNIQUE KEY `uk_callnumber` (`CallNumber`),
  ADD UNIQUE KEY `uk_copyseq_per_book` (`BookID`,`CopySeq`),
  ADD KEY `idx_copies_book` (`BookID`);

--
-- Chỉ mục cho bảng `BORROW_RECORDS`
--
ALTER TABLE `BORROW_RECORDS`
  ADD PRIMARY KEY (`RecordID`),
  ADD KEY `fk_borrow_checkin_by` (`CheckinByUserID`),
  ADD KEY `idx_borrow_copy` (`CopyID`),
  ADD KEY `idx_borrow_open` (`SlipID`,`ReturnDate`);

--
-- Chỉ mục cho bảng `EMPLOYEES`
--
ALTER TABLE `EMPLOYEES`
  ADD PRIMARY KEY (`SystemID`),
  ADD UNIQUE KEY `uk_employeeid` (`EmployeeID`);

--
-- Chỉ mục cho bảng `LIB_SETTINGS`
--
ALTER TABLE `LIB_SETTINGS`
  ADD PRIMARY KEY (`SettingID`),
  ADD KEY `fk_settings_updated_by` (`UpdatedByUserID`);

--
-- Chỉ mục cho bảng `LOAN_SLIPS`
--
ALTER TABLE `LOAN_SLIPS`
  ADD PRIMARY KEY (`SlipID`),
  ADD KEY `fk_slip_patron` (`PatronSysID`),
  ADD KEY `fk_slip_user` (`CheckoutByUserID`);

--
-- Chỉ mục cho bảng `PATRONS`
--
ALTER TABLE `PATRONS`
  ADD PRIMARY KEY (`PatronSysID`),
  ADD UNIQUE KEY `uk_patronid` (`PatronID`);

--
-- Chỉ mục cho bảng `SUBJECTS`
--
ALTER TABLE `SUBJECTS`
  ADD PRIMARY KEY (`SubjectCode`);

--
-- Chỉ mục cho bảng `SYSTEM_USERS`
--
ALTER TABLE `SYSTEM_USERS`
  ADD PRIMARY KEY (`UserID`),
  ADD UNIQUE KEY `uk_username` (`Username`),
  ADD KEY `fk_user_staff` (`StaffSystemID`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `BOOKS`
--
ALTER TABLE `BOOKS`
  MODIFY `BookID` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `BOOK_COPIES`
--
ALTER TABLE `BOOK_COPIES`
  MODIFY `CopyID` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `BORROW_RECORDS`
--
ALTER TABLE `BORROW_RECORDS`
  MODIFY `RecordID` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT cho bảng `EMPLOYEES`
--
ALTER TABLE `EMPLOYEES`
  MODIFY `SystemID` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT cho bảng `LIB_SETTINGS`
--
ALTER TABLE `LIB_SETTINGS`
  MODIFY `SettingID` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT cho bảng `LOAN_SLIPS`
--
ALTER TABLE `LOAN_SLIPS`
  MODIFY `SlipID` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `PATRONS`
--
ALTER TABLE `PATRONS`
  MODIFY `PatronSysID` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT cho bảng `SYSTEM_USERS`
--
ALTER TABLE `SYSTEM_USERS`
  MODIFY `UserID` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `BOOKS`
--
ALTER TABLE `BOOKS`
  ADD CONSTRAINT `fk_books_subject` FOREIGN KEY (`SubjectCode`) REFERENCES `SUBJECTS` (`SubjectCode`) ON UPDATE CASCADE;

--
-- Các ràng buộc cho bảng `BOOK_COPIES`
--
ALTER TABLE `BOOK_COPIES`
  ADD CONSTRAINT `fk_copy_book` FOREIGN KEY (`BookID`) REFERENCES `BOOKS` (`BookID`) ON UPDATE CASCADE;

--
-- Các ràng buộc cho bảng `BORROW_RECORDS`
--
ALTER TABLE `BORROW_RECORDS`
  ADD CONSTRAINT `fk_borrow_checkin_by` FOREIGN KEY (`CheckinByUserID`) REFERENCES `SYSTEM_USERS` (`UserID`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_borrow_copy` FOREIGN KEY (`CopyID`) REFERENCES `BOOK_COPIES` (`CopyID`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_borrow_slip` FOREIGN KEY (`SlipID`) REFERENCES `LOAN_SLIPS` (`SlipID`) ON UPDATE CASCADE;

--
-- Các ràng buộc cho bảng `LIB_SETTINGS`
--
ALTER TABLE `LIB_SETTINGS`
  ADD CONSTRAINT `fk_settings_updated_by` FOREIGN KEY (`UpdatedByUserID`) REFERENCES `SYSTEM_USERS` (`UserID`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Các ràng buộc cho bảng `LOAN_SLIPS`
--
ALTER TABLE `LOAN_SLIPS`
  ADD CONSTRAINT `fk_slip_patron` FOREIGN KEY (`PatronSysID`) REFERENCES `PATRONS` (`PatronSysID`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_slip_user` FOREIGN KEY (`CheckoutByUserID`) REFERENCES `SYSTEM_USERS` (`UserID`) ON UPDATE CASCADE;

--
-- Các ràng buộc cho bảng `SYSTEM_USERS`
--
ALTER TABLE `SYSTEM_USERS`
  ADD CONSTRAINT `fk_user_staff` FOREIGN KEY (`StaffSystemID`) REFERENCES `EMPLOYEES` (`SystemID`) ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
