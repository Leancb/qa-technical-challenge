"""Rebuild the standard JMeter plan using only Python's standard library."""
from pathlib import Path
import xml.etree.ElementTree as E


def prop(parent, name, value, kind="stringProp"):
    E.SubElement(parent, kind, name=name).text = str(value)


def node(parent, tag, name, gui):
    element = E.SubElement(parent, tag, guiclass=gui, testclass=tag,
                           testname=name, enabled="true")
    return element, E.SubElement(parent, "hashTree")


root = E.Element("jmeterTestPlan", version="1.2", properties="5.0", jmeter="5.6.3")
tree = E.SubElement(root, "hashTree")
plan, tree = node(tree, "TestPlan", "Compra de passagem", "TestPlanGui")
prop(plan, "TestPlan.comments", "Carga e pico via properties; vazão em amostras HTTP, sem recursos estáticos.")
args = E.SubElement(plan, "elementProp", name="TestPlan.user_defined_variables", elementType="Arguments")
E.SubElement(args, "collectionProp", name="Arguments.arguments")
group, steps = node(tree, "ThreadGroup", "Usuarios", "ThreadGroupGui")
prop(group, "ThreadGroup.on_sample_error", "startnextloop")
loop = E.SubElement(group, "elementProp", name="ThreadGroup.main_controller", elementType="LoopController", guiclass="LoopControlPanel", testclass="LoopController", enabled="true")
prop(loop, "LoopController.continue_forever", "false", "boolProp")
prop(loop, "LoopController.loops", "${__P(loops,-1)}")
prop(group, "ThreadGroup.num_threads", "${__P(threads,1)}")
prop(group, "ThreadGroup.ramp_time", "${__P(ramp,1)}")
prop(group, "ThreadGroup.scheduler", "true", "boolProp")
prop(group, "ThreadGroup.duration", "${__P(duration,20)}")
prop(group, "ThreadGroup.delay", "0")

cookies, _ = node(steps, "CookieManager", "Sessao independente por compra", "CookiePanel")
E.SubElement(cookies, "collectionProp", name="CookieManager.cookies")
prop(cookies, "CookieManager.clearEachIteration", "true", "boolProp")
timer, _ = node(steps, "ConstantThroughputTimer", "Vazao HTTP compartilhada", "TestBeanGUI")
prop(timer, "calcMode", "4", "intProp")
# Evaluated dynamically: low load, sudden peak, recovery. Throughput unit is per minute.
prop(timer, "throughput", "${__groovy(def elapsed=(System.currentTimeMillis()-org.apache.jmeter.threads.JMeterContextService.getTestStartTime())/1000; def peak=props.getProperty('profile')=='spike' && elapsed>=props.getProperty('spike.start'\u002c'60').toDouble() && elapsed<props.getProperty('spike.end'\u002c'180').toDouble(); (peak ? props.getProperty('peak.rps'\u002c'250') : props.getProperty('rps'\u002c'1')).toDouble()*60)}".replace("\u002c", "\\,"))


def assertion(tree, text, field="Assertion.response_data"):
    check, _ = node(tree, "ResponseAssertion", "Validar " + text, "AssertionGui")
    patterns = E.SubElement(check, "collectionProp", name="Asserion.test_strings")
    prop(patterns, "0", text)
    prop(check, "Assertion.test_field", field)
    prop(check, "Assertion.test_type", "16" if field.endswith("data") else "8", "intProp")
    prop(check, "Assertion.assume_success", "false", "boolProp")


def extract(tree, name, selector, default="MISSING"):
    extractor, _ = node(tree, "HtmlExtractor", "Extrair " + name, "HtmlExtractorGui")
    for key, value in {"refname": name, "expr": selector, "attribute": "value", "match_number": "1", "default": default, "extractor_impl": "JSOUP"}.items():
        prop(extractor, "HtmlExtractor." + key, value)


def http(label, path, data=None, expected=""):
    sampler, children = node(steps, "HTTPSamplerProxy", label, "HttpTestSampleGui")
    arguments = E.SubElement(sampler, "elementProp", name="HTTPsampler.Arguments", elementType="Arguments")
    collection = E.SubElement(arguments, "collectionProp", name="Arguments.arguments")
    for name, value in (data or {}).items():
        arg = E.SubElement(collection, "elementProp", name=name, elementType="HTTPArgument")
        prop(arg, "HTTPArgument.always_encode", "true", "boolProp")
        prop(arg, "Argument.name", name)
        prop(arg, "Argument.value", value)
        prop(arg, "Argument.metadata", "=")
    for key, value in {"domain": "${__P(host,www.blazedemo.com)}", "protocol": "${__P(protocol,https)}", "port": "${__P(port,)}", "path": path, "method": "POST" if data else "GET", "contentEncoding": "UTF-8", "connect_timeout": "10000", "response_timeout": "15000", "implementation": "HttpClient4"}.items():
        prop(sampler, "HTTPSampler." + key, value)
    for key, value in {"follow_redirects": "false", "auto_redirects": "false", "use_keepalive": "true", "image_parser": "false"}.items():
        prop(sampler, "HTTPSampler." + key, value, "boolProp")
    assertion(children, "200", "Assertion.response_code")
    assertion(children, expected)
    return children


http("HTTP_01_Home", "/", expected="Find Flights")
search = http("HTTP_02_Buscar", "/reserve.php", {"fromPort": "Paris", "toPort": "Buenos Aires"}, "Choose This Flight")
for field in ["flight", "price", "airline", "fromPort", "toPort"]:
    extract(search, field, "input[name='" + field + "']")
check, _ = node(search, "JSR223Assertion", "Campos de voo encontrados", "TestBeanGUI")
prop(check, "scriptLanguage", "groovy")
prop(check, "cacheKey", "true")
prop(check, "script", "if (['flight','price','airline','fromPort','toPort'].any { vars.get(it) == 'MISSING' }) { AssertionResult.setFailure(true); AssertionResult.setFailureMessage('Correlacao de voo ausente'); }")
purchase = http("HTTP_03_Selecionar", "/purchase.php", {field: "${" + field + "}" for field in ["flight", "price", "airline", "fromPort", "toPort"]}, "Purchase Flight")
extract(purchase, "token", "input[name='_token']", "")
http("HTTP_04_Confirmar", "/confirmation.php", {"_token": "${token}", "inputName": "QA Test", "address": "Test Street 123", "city": "Test City", "state": "RS", "zipCode": "90000000", "cardType": "visa", "creditCardNumber": "4111111111111111", "creditCardMonth": "12", "creditCardYear": "2030", "nameOnCard": "QA Test"}, "Thank you for your purchase today!")

E.indent(root, space="  ")
E.ElementTree(root).write(Path(__file__).with_name("purchase.jmx"), encoding="UTF-8", xml_declaration=True)
