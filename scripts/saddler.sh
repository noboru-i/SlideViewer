#!/usr/bin/env bash

set -eu

echo "********************"
echo "* save outputs     *"
echo "********************"

mkdir -p "$CIRCLE_ARTIFACTS"
cp -v "app/build/reports/checkstyle/checkstyle.xml" "$CIRCLE_ARTIFACTS/"
cp -v "app/build/reports/findbugs/findbugs.xml" "$CIRCLE_ARTIFACTS/"
cp -v "app/build/reports/pmd/pmd.xml" "$CIRCLE_ARTIFACTS/"
cp -v "app/build/reports/pmd/cpd.xml" "$CIRCLE_ARTIFACTS/"
cp -v "app/build/reports/lint-results.xml" "$CIRCLE_ARTIFACTS/"

if [ -z "${CI_PULL_REQUEST+UNDEF}" ]; then
    # when not pull request
    REPORTER=Saddler::Reporter::Github::CommitReviewComment
else
    REPORTER=Saddler::Reporter::Github::PullRequestReviewComment
fi

echo "********************"
echo "* checkstyle       *"
echo "********************"
cat app/build/reports/checkstyle/checkstyle.xml \
    | checkstyle_filter-git diff origin/master \
    | saddler report --require saddler/reporter/github --reporter $REPORTER

echo "********************"
echo "* findbugs         *"
echo "********************"
cat app/build/reports/findbugs/findbugs.xml \
    | findbugs_translate_checkstyle_format translate \
    | checkstyle_filter-git diff origin/master \
    | saddler report --require saddler/reporter/github --reporter $REPORTER

echo "********************"
echo "* PMD              *"
echo "********************"
cat app/build/reports/pmd/pmd.xml \
    | pmd_translate_checkstyle_format translate \
    | checkstyle_filter-git diff origin/master \
    | saddler report --require saddler/reporter/github --reporter $REPORTER

echo "********************"
echo "* PMD-CPD          *"
echo "********************"
cat app/build/reports/pmd/cpd.xml \
    | pmd_translate_checkstyle_format translate --cpd-translate \
    | checkstyle_filter-git diff origin/master \
    | saddler report --require saddler/reporter/github --reporter $REPORTER

echo "********************"
echo "* android lint     *"
echo "********************"
cat app/build/reports/lint-results.xml \
    | android_lint_translate_checkstyle_format translate \
    | checkstyle_filter-git diff origin/master \
    | saddler report --require saddler/reporter/github --reporter $REPORTER
