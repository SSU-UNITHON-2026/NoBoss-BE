INSERT INTO projects (
    team_name,
    subject_name,
    project_topic,
    deadline,
    description
) VALUES (
    '서비스디자인 캡스톤',
    '서비스디자인 캡스톤',
    '캠퍼스 중고거래 앱 UX 개선',
    DATE '2026-12-11',
    '교내 중고거래 과정의 불편함을 개선하는 UX 프로젝트'
);

INSERT INTO tasks (
    project_id,
    stage,
    stage_name,
    title,
    owner,
    due_date,
    done
) VALUES
    (1, 2, '리서치', '인터뷰 스크립트 확정', '윤세아', DATE '2026-08-23', TRUE),
    (1, 2, '리서치', '사용자 인터뷰 5명 진행', '윤세아', DATE '2026-08-27', FALSE),
    (1, 2, '리서치', '설문 문항 교차 검토', '공동', DATE '2026-08-26', FALSE);
