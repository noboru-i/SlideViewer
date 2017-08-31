# github comment settings
github.dismiss_out_of_range_messages

# for PR
if github.pr_title.include?('[WIP]') || github.pr_labels.include?('WIP')
  warn('PR is classed as Work in Progress')
end

# Warn when there is a big PR
warn('a large PR') if git.lines_of_code > 300

# checkstyle
checkstyle_format.base_path = Dir.pwd
checkstyle_format.report 'app/build/reports/checkstyle/checkstyle.xml'

# # Findbugs
# findbugs.gradle_task = "app:findbugs"
# findbugs.report_file = "app/build/reports/findbugs/findbugs.xml"
# findbugs.report(true)
#
# # Android Lint
# android_lint.gradle_task = "app:lint"
# android_lint.report_file = "app/build/reports/lint-results.xml"
# android_lint.filtering = true
# android_lint.lint(inline_mode: true)
