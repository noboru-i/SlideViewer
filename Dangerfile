# github comment settings
github.dismiss_out_of_range_messages

# for PR
if github.pr_title.include?('[WIP]') || github.pr_labels.include?('WIP')
  warn('PR is classed as Work in Progress')
end

# Warn when there is a big PR
warn('a large PR') if git.lines_of_code > 300

checkstyle_format.base_path = Dir.pwd

# checkstyle
checkstyle_format.report 'app/build/reports/checkstyle/checkstyle.xml'

# Findbugs
require 'findbugs_translate_checkstyle_format'
findbugs_xml = ::FindbugsTranslateCheckstyleFormat::Script.translate(File.read('app/build/reports/findbugs/findbugs.xml'))
checkstyle_format.report_by_text findbugs_xml

# TODO: below methods need Android SDK
# # Android Lint
# android_lint.gradle_task = "app:lint"
# android_lint.report_file = "app/build/reports/lint-results.xml"
# android_lint.filtering = true
# android_lint.lint(inline_mode: true)
