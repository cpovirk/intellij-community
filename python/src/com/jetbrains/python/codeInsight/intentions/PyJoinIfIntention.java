package com.jetbrains.python.codeInsight.intentions;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: catherine
 */
public class PyJoinIfIntention extends BaseIntentionAction {
  @NotNull
  public String getFamilyName() {
    return PyBundle.message("INTN.join.if");
  }

  @NotNull
  public String getText() {
    return PyBundle.message("INTN.join.if.text");
  }

  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    PyIfStatement expression =
      PsiTreeUtil.getParentOfType(file.findElementAt(editor.getCaretModel().getOffset()), PyIfStatement.class);

    PyIfStatement ifStatement = getIfStatement(expression);
    PyStatement firstStatement = getFirstStatement(ifStatement);

    if (firstStatement != null) {
      PyStatementList stList = ((PyIfStatement)firstStatement).getIfPart().getStatementList();
      if (stList != null)
        if (stList.getStatements().length != 0)
          return true;
    }
    return false;
  }

  public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    PyIfStatement expression =
          PsiTreeUtil.getParentOfType(file.findElementAt(editor.getCaretModel().getOffset()), PyIfStatement.class);
    PyIfStatement ifStatement = getIfStatement(expression);

    PyStatement firstStatement = getFirstStatement(ifStatement);

    if (firstStatement != null && firstStatement instanceof PyIfStatement) {
      PyExpression condition = ((PyIfStatement)firstStatement).getIfPart().getCondition();
      PyElementGenerator elementGenerator = PyElementGenerator.getInstance(project);
      PyExpression newCondition = elementGenerator.createExpressionFromText(ifStatement.getIfPart().getCondition().getText() + " and " + condition.getText());
      ifStatement.getIfPart().getCondition().replace(newCondition);

      PyStatementList stList = ((PyIfStatement)firstStatement).getIfPart().getStatementList();
      PyStatementList ifStatementList = ifStatement.getIfPart().getStatementList();
      ifStatementList.replace(stList);
    }
  }

  @Nullable
  private static PyStatement getFirstStatement(PyIfStatement ifStatement) {
    PyStatement firstStatement = null;
    if (ifStatement != null) {
      PyStatementList stList = ifStatement.getIfPart().getStatementList();
      if (stList != null) {
        if (stList.getStatements().length != 0) {
          firstStatement = stList.getStatements()[0];
        }
      }
    }
    return firstStatement;
  }

  @Nullable
  private static PyIfStatement getIfStatement(PyIfStatement expression) {
    while (expression != null) {
      PyStatementList stList = expression.getIfPart().getStatementList();
      if (stList != null) {
        if (stList.getStatements().length != 0) {
          PyStatement firstStatement = stList.getStatements()[0];
          if (firstStatement instanceof PyIfStatement) {
            break;
          }
        }
      }
      expression = PsiTreeUtil.getParentOfType(expression, PyIfStatement.class);
    }
    return expression;
  }
}
