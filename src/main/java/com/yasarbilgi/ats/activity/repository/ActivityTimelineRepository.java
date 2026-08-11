package com.yasarbilgi.ats.activity.repository;

import com.yasarbilgi.ats.activity.dto.response.ActivityResponseDto;
import com.yasarbilgi.ats.activity.entity.ActivityType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ActivityTimelineRepository {

    private static final String STAGE_CHANGES = """
            SELECT 'STAGE_CHANGE' AS activity_type,
                   history.id AS reference_id,
                   process.id AS candidate_process_id,
                   CASE WHEN history.from_stage_id IS NULL THEN 'Sürece eklendi'
                        ELSE 'Aşama değiştirildi' END AS title,
                   CASE WHEN history.from_stage_id IS NULL THEN target_stage.name
                        ELSE source_stage.name || ' → ' || target_stage.name END AS description,
                   target_stage.stage_type AS status,
                   history.created_at AS occurred_at,
                   NULL::timestamptz AS target_at,
                   history.created_by AS performed_by
              FROM candidate_process_stage_history history
              JOIN candidate_processes process ON process.id = history.candidate_process_id
              JOIN positions position ON position.id = process.position_id
              JOIN pipeline_stages target_stage ON target_stage.id = history.to_stage_id
              LEFT JOIN pipeline_stages source_stage ON source_stage.id = history.from_stage_id
             WHERE history.company_id = :companyId
               AND process.candidate_id = :candidateId
               AND (:companyWide OR position.department_id IN (:departmentIds))
            """;

    private static final String NOTES = """
            SELECT 'NOTE' AS activity_type,
                   note.id AS reference_id,
                   note.candidate_process_id AS candidate_process_id,
                   'Aday notu eklendi' AS title,
                   note.content AS description,
                   NULL::varchar AS status,
                   note.created_at AS occurred_at,
                   NULL::timestamptz AS target_at,
                   note.created_by AS performed_by
              FROM candidate_notes note
              LEFT JOIN candidate_processes process ON process.id = note.candidate_process_id
              LEFT JOIN positions position ON position.id = process.position_id
             WHERE note.company_id = :companyId
               AND note.candidate_id = :candidateId
               AND note.active = true
               AND (:companyWide OR position.department_id IN (:departmentIds))
            """;

    private static final String INTERACTIONS = """
            SELECT 'INTERACTION' AS activity_type,
                   interaction.id AS reference_id,
                   interaction.candidate_process_id AS candidate_process_id,
                   interaction.channel || ' iletişimi' AS title,
                   interaction.summary AS description,
                   interaction.direction AS status,
                   interaction.occurred_at AS occurred_at,
                   NULL::timestamptz AS target_at,
                   interaction.created_by AS performed_by
              FROM candidate_interactions interaction
              LEFT JOIN candidate_processes process ON process.id = interaction.candidate_process_id
              LEFT JOIN positions position ON position.id = process.position_id
             WHERE interaction.company_id = :companyId
               AND interaction.candidate_id = :candidateId
               AND interaction.active = true
               AND (:companyWide OR position.department_id IN (:departmentIds))
            """;

    private static final String INTERVIEWS = """
            SELECT 'INTERVIEW' AS activity_type,
                   interview.id AS reference_id,
                   process.id AS candidate_process_id,
                   interview.type || ' görüşmesi' AS title,
                   interview.mode AS description,
                   interview.status AS status,
                   interview.created_at AS occurred_at,
                   interview.scheduled_at AS target_at,
                   interview.created_by AS performed_by
              FROM interviews interview
              JOIN candidate_processes process ON process.id = interview.candidate_process_id
              JOIN positions position ON position.id = process.position_id
             WHERE interview.company_id = :companyId
               AND process.candidate_id = :candidateId
               AND interview.active = true
               AND (:companyWide OR position.department_id IN (:departmentIds))
            """;

    private static final String FOLLOW_UPS = """
            SELECT 'FOLLOW_UP' AS activity_type,
                   follow_up.id AS reference_id,
                   follow_up.candidate_process_id AS candidate_process_id,
                   follow_up.title AS title,
                   follow_up.description AS description,
                   follow_up.status AS status,
                   follow_up.created_at AS occurred_at,
                   follow_up.due_at AS target_at,
                   follow_up.created_by AS performed_by
              FROM candidate_follow_ups follow_up
              LEFT JOIN candidate_processes process ON process.id = follow_up.candidate_process_id
              LEFT JOIN positions position ON position.id = process.position_id
             WHERE follow_up.company_id = :companyId
               AND follow_up.candidate_id = :candidateId
               AND follow_up.active = true
               AND (:companyWide OR position.department_id IN (:departmentIds))
            """;

    private static final RowMapper<ActivityResponseDto> ROW_MAPPER = (result, rowNumber) ->
            new ActivityResponseDto(
                    ActivityType.valueOf(result.getString("activity_type")),
                    result.getLong("reference_id"),
                    result.getObject("candidate_process_id", Long.class),
                    result.getString("title"),
                    result.getString("description"),
                    result.getString("status"),
                    result.getTimestamp("occurred_at").toInstant(),
                    toInstant(result.getTimestamp("target_at")),
                    result.getObject("performed_by", Long.class)
            );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public TimelinePage findTimeline(Long companyId, Long candidateId, ActivityType type,
                                     int page, int size, boolean companyWide,
                                     Set<Long> departmentIds) {
        String source = sourceFor(type);
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("companyId", companyId)
                .addValue("candidateId", candidateId)
                .addValue("companyWide", companyWide)
                .addValue("departmentIds", departmentIds.isEmpty() ? List.of(-1L) : departmentIds)
                .addValue("limit", size)
                .addValue("offset", (long) page * size);

        List<ActivityResponseDto> content = jdbcTemplate.query(
                "SELECT * FROM (" + source + ") timeline "
                        + "ORDER BY occurred_at DESC, reference_id DESC LIMIT :limit OFFSET :offset",
                parameters,
                ROW_MAPPER
        );
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM (" + source + ") timeline",
                parameters,
                Long.class
        );
        return new TimelinePage(content, total == null ? 0 : total);
    }

    private String sourceFor(ActivityType type) {
        if (type == null) {
            return String.join(" UNION ALL ",
                    STAGE_CHANGES, NOTES, INTERACTIONS, INTERVIEWS, FOLLOW_UPS);
        }
        return switch (type) {
            case STAGE_CHANGE -> STAGE_CHANGES;
            case NOTE -> NOTES;
            case INTERACTION -> INTERACTIONS;
            case INTERVIEW -> INTERVIEWS;
            case FOLLOW_UP -> FOLLOW_UPS;
        };
    }

    private static java.time.Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    public record TimelinePage(List<ActivityResponseDto> content, long totalElements) {
    }
}
