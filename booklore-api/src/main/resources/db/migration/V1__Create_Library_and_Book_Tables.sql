CREATE TABLE IF NOT EXISTS library
(
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(255) UNIQUE NOT NULL,
    sort  VARCHAR(255)        NULL,
    icon  VARCHAR(64)         NOT NULL,
    paths TEXT
);

CREATE TABLE IF NOT EXISTS book
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name      VARCHAR(255)  NOT NULL,
    library_id     BIGINT        NOT NULL,
    path           VARCHAR(1000) NOT NULL,
    last_read_time TIMESTAMP     NULL,
    added_on       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_library FOREIGN KEY (library_id) REFERENCES library (id) ON DELETE CASCADE,
    CONSTRAINT unique_file_library UNIQUE (file_name, library_id),
    INDEX idx_library_id (library_id),
    INDEX idx_last_read_time (last_read_time)
);

CREATE TABLE IF NOT EXISTS book_metadata
(
    book_id        BIGINT NOT NULL PRIMARY KEY,
    google_book_id VARCHAR(255) UNIQUE,
    title          VARCHAR(255),
    subtitle       VARCHAR(255),
    publisher      VARCHAR(255),
    published_date DATE,
    description    TEXT,
    isbn_13        VARCHAR(13),
    isbn_10        VARCHAR(10),
    page_count     INT,
    thumbnail      VARCHAR(1000),
    language       VARCHAR(10),
    CONSTRAINT fk_book_metadata FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS author
(
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    CONSTRAINT unique_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS category
(
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS book_viewer_setting
(
    book_id         BIGINT PRIMARY KEY,
    page_number     INT         DEFAULT 1,
    zoom            VARCHAR(16) DEFAULT 'page-fit',
    sidebar_visible BOOLEAN     DEFAULT false,
    spread          VARCHAR(16) DEFAULT 'odd',
    CONSTRAINT fk_book_viewer_setting FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS book_metadata_category_mapping
(
    book_id     BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, category_id),
    CONSTRAINT fk_book_metadata_category_mapping_book FOREIGN KEY (book_id) REFERENCES book_metadata (book_id) ON DELETE CASCADE,
    CONSTRAINT fk_book_metadata_category_mapping_category FOREIGN KEY (category_id) REFERENCES category (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS book_metadata_author_mapping
(
    book_id   BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, author_id),
    CONSTRAINT fk_book_metadata_author_mapping_book FOREIGN KEY (book_id) REFERENCES book_metadata (book_id) ON DELETE CASCADE,
    CONSTRAINT fk_book_metadata_author_mapping_author FOREIGN KEY (author_id) REFERENCES author (id) ON DELETE CASCADE,
    INDEX idx_book_metadata_id (book_id),
    INDEX idx_author_id (author_id)
);


CREATE TABLE IF NOT EXISTS shelf
(
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    sort VARCHAR(255) NULL,
    icon VARCHAR(64)  NOT NULL
);

CREATE TABLE IF NOT EXISTS book_shelf_mapping
(
    book_id  BIGINT NOT NULL,
    shelf_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, shelf_id),
    CONSTRAINT fk_book_shelf_mapping_book FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE,
    CONSTRAINT fk_book_shelf_mapping_shelf FOREIGN KEY (shelf_id) REFERENCES shelf (id) ON DELETE CASCADE
);