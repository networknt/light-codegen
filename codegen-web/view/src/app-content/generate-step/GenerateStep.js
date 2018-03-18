/**
 * Created by Nicholas Azar on 2017-07-05.
 */

import React, {Component} from 'react';
import {Row, Col} from 'antd';
import Highlight from 'react-highlight'

import './GenerateStep.less';
import {API_SERVER} from "../../AppConstants";
import {AppActions} from "../../AppActions";

class GenerateStep extends Component {

    // onMenuItemClick = (e) => {
    //     if (e.key === 'add-another') {
    //         this.props.onAddAnother();
    //     }
    // };

    // generate_button_menu = (
    //     <Menu onClick={this.onMenuItemClick}>
    //         <Menu.Item key="add-another">Add Another</Menu.Item>
    //     </Menu>
    // );

    render() {

        return (
            <div style={{padding: '20px'}}>
                <Row type="flex" justify="center">
                    <Col xs={24} sm={18} md={14} lg={12} xl={6}>
                        <h2 style={{color: 'rgba(0, 0, 0, 0.5)'}}>Generate</h2>
                        <p>
                            Review the selected uploaded files and click generate
                        </p>
                    </Col>
                </Row>
                <div style={{height: '20px'}}/>
                <Row type="flex" justify="center">
                    <Col span={10}>
                        <Highlight className="prettyprint lang-json">{this.props.initValues.schema.schemaContent}</Highlight>
                    </Col>
                    <Col span={2}>
                    </Col>
                    <Col span={10}>
                        <Highlight className="prettyprint lang-json">{this.props.initValues.config.configContent}</Highlight>
                    </Col>
                </Row>
                <Row type="flex" justify="left">
                    <Col span={2} offset={21}>
                        {/*Empty Iframe target used to prevent a page refresh when submitting the form.*/}
                        <iframe name="nothing" style={{display: "none"}} title="nothing"></iframe>
                        <form method="post" action={API_SERVER + "/api/zip"} target="nothing">
                            <input type="hidden" name="host" value={AppActions.API_HOST}/>
                            <input type="hidden" name="service" value={AppActions.API_SERVICE}/>
                            <input type="hidden" name="action" value="generate"/>
                            <input type="hidden" name="version" value={AppActions.API_VERSION}/>

                            {/*These can be programmatically added in the future when the ui supports multiple generators. Server side already does.*/}
                            <input type="hidden" name="generator.model" value={this.props.initValues.schema.schemaContent}/>
                            <input type="hidden" name="generator.config" value={this.props.initValues.config.configContent} />
                            <input type="hidden" name="generator.framework" value={this.props.initValues.schema.framework} />
                            <button type="submit" className="ant-btn ant-btn-primary generate-button">Download!</button>
                        </form>

                        {/*<Dropdown.Button onClick={this.onGenerateButtonClick} overlay={this.generate_button_menu} className="generate-button" type="primary" size="large">*/}
                            {/*Generate!*/}
                        {/*</Dropdown.Button>*/}
                    </Col>
                </Row>
            </div>
        )
    }
}

export default GenerateStep;