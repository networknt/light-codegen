/**
 * Created by Nicholas Azar on 2017-07-05.
 */

import React, {Component} from 'react';
import {Row, Col, Button} from 'antd';
import Highlight from 'react-highlight'

import './GenerateStep.less';
import {AppServices} from "../../AppServices";

class GenerateStep extends Component {

    // onMenuItemClick = (e) => {
    //     if (e.key === 'add-another') {
    //         this.props.onAddAnother();
    //     }
    // };

    onGenerateButtonClick = () => {
        AppServices.generate(this.props.initValues.schema.framework, JSON.parse(this.props.initValues.schema.schemaContent), JSON.parse(this.props.initValues.config.configContent)).then((response) => {
            const blob = new Blob([response.data]);
            const filename = this.props.initValues.schema.framework + '.zip';

            if (typeof window.navigator.msSaveBlob !== 'undefined') {
                window.navigator.msSaveBlob(blob, filename);
            } else {
                const url = window.URL.createObjectURL(blob);
                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', filename);
                document.body.appendChild(link);
                link.click();
            }
        });
    };

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

                        <Button onClick={this.onGenerateButtonClick} className="generate-button" type="primary">Generate</Button>

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