CREATE TABLE team (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE team_member (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(50),
    team_id INT,
    CONSTRAINT fk_team_member_team
        FOREIGN KEY (team_id)
        REFERENCES team(id)
);

CREATE TABLE project (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE project_team (
    project_id INT NOT NULL,
    team_id INT NOT NULL,
    PRIMARY KEY (project_id, team_id),
    CONSTRAINT fk_project
        FOREIGN KEY (project_id)
        REFERENCES project(id),
    CONSTRAINT fk_team
        FOREIGN KEY (team_id)
        REFERENCES team(id)
);

















SELECT * FROM dbo.project
