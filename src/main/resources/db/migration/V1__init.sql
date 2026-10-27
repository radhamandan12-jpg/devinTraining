CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE TABLE loan_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    applicant_id BIGINT NOT NULL,
    requested_amount DECIMAL(15,2) NOT NULL,
    term_months INT NOT NULL,
    loan_purpose VARCHAR(30) NOT NULL,
    annual_income DECIMAL(15,2) NOT NULL,
    monthly_debt_payments DECIMAL(15,2) NOT NULL,
    employment_status VARCHAR(30) NOT NULL,
    years_employed INT,
    credit_score INT,
    applicant_statement TEXT,
    status VARCHAR(30) NOT NULL,
    ai_summary TEXT,
    ai_missing_info TEXT,
    ai_risk_notes TEXT,
    decision_explanation TEXT,
    decided_by VARCHAR(255),
    decided_at DATETIME(6),
    decision_notes TEXT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_app_applicant FOREIGN KEY (applicant_id) REFERENCES users (id)
);

CREATE TABLE documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    content LONGBLOB,
    extracted_data TEXT,
    uploaded_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_doc_application FOREIGN KEY (application_id) REFERENCES loan_applications (id)
);

CREATE TABLE rule_evaluations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    rule_name VARCHAR(100) NOT NULL,
    severity VARCHAR(10) NOT NULL,
    passed BIT NOT NULL,
    detail TEXT,
    evaluated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_rule_application FOREIGN KEY (application_id) REFERENCES loan_applications (id)
);

CREATE TABLE communications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    channel VARCHAR(50) NOT NULL,
    subject TEXT,
    body TEXT,
    drafted_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_comm_application FOREIGN KEY (application_id) REFERENCES loan_applications (id)
);

CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT,
    actor VARCHAR(100) NOT NULL,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    created_at DATETIME(6) NOT NULL
);

CREATE INDEX idx_audit_application ON audit_logs (application_id);
