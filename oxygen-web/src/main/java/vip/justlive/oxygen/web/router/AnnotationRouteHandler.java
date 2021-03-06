/*
 *  Copyright (C) 2019 justlive1
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License
 *  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing permissions and limitations under
 *  the License.
 */
package vip.justlive.oxygen.web.router;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ServiceLoaderUtils;
import vip.justlive.oxygen.web.bind.DataBinder;
import vip.justlive.oxygen.web.bind.ParamBinder;
import vip.justlive.oxygen.web.result.Result;

/**
 * 注解路由处理
 *
 * @author wubo
 */
public class AnnotationRouteHandler implements RouteHandler {

  private static final List<ParamBinder> BINDERS;

  static {
    BINDERS = ServiceLoaderUtils.loadServices(ParamBinder.class);
    Collections.sort(BINDERS);
  }

  private final Object router;
  private final Method proxyMethod;
  private final Method method;
  private final DataBinder[] dataBinders;

  public AnnotationRouteHandler(Object router, Method proxyMethod, Method method) {
    this.router = router;
    this.proxyMethod = proxyMethod;
    this.method = method;
    this.dataBinders = new DataBinder[method.getParameterCount()];
    parse();
  }

  private void parse() {
    Parameter[] parameters = method.getParameters();
    for (int i = 0; i < parameters.length; i++) {
      for (ParamBinder binder : BINDERS) {
        if (binder.supported(parameters[i])) {
          dataBinders[i] = binder.build(parameters[i]);
          break;
        }
      }
    }
  }

  @Override
  public void handle(RoutingContext ctx) {
    Object[] args = new Object[dataBinders.length];
    for (int i = 0; i < dataBinders.length; i++) {
      args[i] = dataBinders[i].getFunc().apply(ctx);
    }
    Object result;
    try {
      result = proxyMethod.invoke(router, args);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw Exceptions.wrap(e);
    }
    if (Result.class.isAssignableFrom(method.getReturnType())) {
      ctx.response().setResult((Result) result);
    } else if (result != null) {
      ctx.response().setResult(Result.json(result));
    }
  }

}
